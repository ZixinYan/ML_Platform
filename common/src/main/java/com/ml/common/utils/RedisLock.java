package com.ml.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;


/**
 * Redis分布式锁工具类
 */
@Component
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 尝试获取锁
     * @param key 锁的键
     * @param value 锁的唯一值（用于识别锁的持有者）
     * @param expireTime 锁的过期时间（毫秒）
     * @param waitTime 等待获取锁的最大时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String value, long expireTime, long waitTime) {
        long startTime = System.currentTimeMillis();
        while (true) {
            // 使用SET命令的NX（不存在才设置）和PX（过期时间）参数
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(expireTime));
            if (Boolean.TRUE.equals(success)) {
                return true;
            }

            // 检查是否超时
            if (System.currentTimeMillis() - startTime > waitTime) {
                return false;
            }

            // 短暂休眠后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 释放锁
     * @param key 锁的键
     * @param value 锁的唯一值（必须与获取时的value一致）
     */
    public void unlock(String key, String value) {
        // 使用Lua脚本确保释放锁的原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(key), value);
    }
}