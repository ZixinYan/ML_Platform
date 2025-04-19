package com.ml.blog.cache;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ml.blog.anno.CacheAdd;
import com.ml.blog.anno.CacheDelete;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.ml.blog.constant.RedisConstants.CACHE_NULL_TTL;

@Slf4j
@Aspect
@Component
@Order(1)
public class CacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final com.google.common.cache.Cache<String, Object> localCache =
            com.google.common.cache.CacheBuilder.newBuilder()
                    .maximumSize(1024)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    private final Map<String, Set<String>> prefixIndex = new ConcurrentHashMap<>();

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 缓存插入与读取
     */
    @Around("@annotation(cacheAdd)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, CacheAdd cacheAdd) throws Throwable {
        String key = parseSpELKey(joinPoint, cacheAdd.key());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();

        // 1. 本地缓存查询
        Object local = localCache.getIfPresent(key);
        if (local != null) {
            log.info("[Cache] 本地缓存命中: {}", key);
            return local;
        }

        // 2. Redis 缓存查询
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            Object redisVal = JSONUtil.toBean(json, returnType);
            putLocalCache(key, redisVal);
            log.info("[Cache] Redis缓存命中: {}", key);
            return redisVal;
        }

        // 3. 执行业务逻辑并缓存
        Object result = joinPoint.proceed();
        if (result == null) {
            // 设置空值，防止穿透
            redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        String resultJson = JSONUtil.toJsonStr(result);
        redisTemplate.opsForValue().set(key, resultJson, cacheAdd.ttl(), TimeUnit.MINUTES);
        putLocalCache(key, result);
        return result;
    }


    /**
     * 处理缓存清除逻辑
     */
    @Around("@annotation(cacheDelete)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint, CacheDelete cacheDelete) throws Throwable {
        // 解析多个缓存 key
        String[] spelKeys = cacheDelete.keys();
        String[] parsedKeys = Arrays.stream(spelKeys)
                .map(k -> parseSpELKey(joinPoint, k))
                .toArray(String[]::new);
        if (cacheDelete.beforeInvocation()) {
            cleanCache(parsedKeys);
        }
        Object result = joinPoint.proceed();
        if (!cacheDelete.beforeInvocation()) {
            cleanCache(parsedKeys);
        }
        return result;
    }


    /**
     * 清理缓存
     * @param keys 缓存的 key
     */
    private void cleanCache(String[] keys) throws IOException {
        // 清理 Redis 缓存
        for (String key : keys) {
            if (key.contains("*")) {
                String prefix = key.replace("*", "");
                deleteRedisByPrefix(prefix);
            } else {
                deleteRedisByKey(key);
            }
        }


        // 清理本地缓存
        for (String key : keys) {
            if (key.contains("*")) {
                String prefix = key.replace("*", "");
                clearLocalByPrefix(prefix);
            } else {
                clearLocalByKey(key);
            }
        }

        log.info("[Cache] 缓存清理完成: {}", Arrays.toString(keys));
    }

    /**
     * SpEL 解析缓存 key
     */
    private String parseSpELKey(ProceedingJoinPoint joinPoint, String spel) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return parser.parseExpression(spel).getValue(context, String.class);
    }

    /**
     * 写入本地缓存并记录索引
     */
    private void putLocalCache(String key, Object value) {
        localCache.put(key, value);
        String prefix = extractPrefix(key);
        prefixIndex.computeIfAbsent(prefix, k -> ConcurrentHashMap.newKeySet()).add(key);
    }

    /**
     * 清除本地缓存
     */
    private void clearLocalByKey(String key) {
        localCache.invalidate(key);
        String prefix = extractPrefix(key);
        Set<String> keys = prefixIndex.getOrDefault(prefix, Collections.emptySet());
        keys.remove(key);
        if (keys.isEmpty()) {
            prefixIndex.remove(prefix);
        }
        log.info("[Cache] 清除本地缓存: {}", key);
    }


    /**
     * 本地缓存清除 - 根据前缀模糊匹配
     */
    private void clearLocalByPrefix(String prefix) {
        Set<String> keys = prefixIndex.getOrDefault(prefix, Collections.emptySet());
        for (String key : keys) {
            localCache.invalidate(key); // 强制清除本地缓存
        }
        prefixIndex.remove(prefix);

        // 模糊匹配前缀
        Set<String> fuzzyKeys = localCache.asMap().keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .collect(Collectors.toSet());
        localCache.invalidateAll(fuzzyKeys); // 清除所有以 prefix 为前缀的缓存
    }


    /**
     * Redis 缓存清除
     */
    private void deleteRedisByKey(String key) {
        redisTemplate.delete(key);
        log.info("[Redis] 删除缓存: {}", key);
    }

    /**
     * Redis 缓存清除，基于前缀模糊匹配
     */
    private void deleteRedisByPrefix(String prefix) throws IOException {
        final String KEY_PATTERN = prefix + "*";
        Cursor<byte[]> cursor = redisTemplate.execute(new RedisCallback<Cursor<byte[]>>() {
            @Override
            public Cursor<byte[]> doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return  redisConnection.scan(new ScanOptions.ScanOptionsBuilder()
                        .match(KEY_PATTERN)
                        .count(1000)
                        .build());
            }
        });

        try {
            while (cursor.hasNext()) {
                byte[] keyBytes = cursor.next();
                String key = new String(keyBytes);
                if (key.startsWith(prefix)) {
                    redisTemplate.delete(key);
                }
            }
        } finally {
            cursor.close(); // 关闭游标
        }
    }

    /**
     * 提取缓存 key 的前缀
     * @param key 缓存的 key
     * @return 前缀
     */
    private String extractPrefix(String key) {
        int index = key.lastIndexOf(":");
        return index >= 0 ? key.substring(0, index + 1) : key;
    }
}
