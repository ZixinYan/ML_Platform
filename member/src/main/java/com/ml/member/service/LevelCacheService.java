package com.ml.member.service;

import com.ml.member.entity.MemberLevelEntity;

import java.util.Map;

public interface LevelCacheService {
    void initLevelCache();
    void refreshLevelCache();

    Map<Integer, MemberLevelEntity> getLevelCache();
}
