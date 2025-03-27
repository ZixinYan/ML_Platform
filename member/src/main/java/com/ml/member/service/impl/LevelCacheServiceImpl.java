package com.ml.member.service.impl;

import com.ml.member.dao.MemberLevelDao;
import com.ml.member.entity.MemberLevelEntity;
import com.ml.member.service.LevelCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LevelCacheServiceImpl implements LevelCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberLevelDao memberLevelDao;

    private static final String LEVEL_KEY = "level_mapping"; // Redis key

    /**
     * 初始化缓存：从数据库加载等级信息到 Redis
     */
    @PostConstruct
    public void initLevelCache() {
        List<MemberLevelEntity> levels = memberLevelDao.getAllLevels(); // 查询所有等级信息
        if (levels.isEmpty()) return;

        // 清空 Redis 旧缓存
        redisTemplate.delete(LEVEL_KEY);

        // 存入 Redis（ZSET：score=growthPoint, value=levelId）
        for (MemberLevelEntity level : levels) {
            redisTemplate.opsForZSet().add(LEVEL_KEY, level.getId().toString(), level.getGrowthPoint());
        }
    }

    /**
     * 定时刷新缓存，每天凌晨 3 点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshLevelCache() {
        initLevelCache();
    }

    /**
     * 获取缓存中的等级映射
     * @return Map<growthPoint, MemberLevelEntity>
     */
    @Override
    public Map<Integer, MemberLevelEntity> getLevelCache() {
        Set<ZSetOperations.TypedTuple<String>> cachedLevels = redisTemplate.opsForZSet().rangeWithScores(LEVEL_KEY, 0, -1);
        if (cachedLevels == null || cachedLevels.isEmpty()) {
            return Collections.emptyMap();
        }

        // 从 Redis 取出的数据是 (levelId, growthPoint)，需要转换回 MemberLevelEntity
        List<Long> levelIds = cachedLevels.stream()
                .map(tuple -> Long.parseLong(tuple.getValue()))
                .collect(Collectors.toList());

        // 根据 ID 查询等级详情
        List<MemberLevelEntity> levels = memberLevelDao.selectBatchIds(levelIds);
        Map<Long, MemberLevelEntity> levelMap = levels.stream()
                .collect(Collectors.toMap(MemberLevelEntity::getId, level -> level));

        // 构造最终的 Map<growthPoint, MemberLevelEntity>
        return cachedLevels.stream()
                .filter(tuple -> levelMap.containsKey(Long.parseLong(tuple.getValue())))
                .collect(Collectors.toMap(
                        tuple -> tuple.getScore().intValue(),
                        tuple -> levelMap.get(Long.parseLong(tuple.getValue())),
                        (a, b) -> b, // 解决可能的重复 key
                        TreeMap::new // 保证有序
                ));
    }
}
