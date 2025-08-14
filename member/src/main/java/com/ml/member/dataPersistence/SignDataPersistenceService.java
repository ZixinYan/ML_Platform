package com.ml.member.dataPersistence;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.common.utils.DateUtils;
import com.ml.member.entity.MemberDaySignEntity;
import com.ml.member.entity.MemberSignInfoEntity;
import com.ml.member.service.MemberDaySignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每月1日0点执行：持久化上月数据到数据库
 * 对应功能：签到功能与签到日历功能
 * 数据源：com/ml/member/service/impl/MemberDaySignServiceImpl.java
 */

@Slf4j
@Component
public class SignDataPersistenceService {

    private final StringRedisTemplate redisTemplate;
    private final MemberDaySignService memberDaySignService;
    private final ObjectMapper objectMapper;
    List<MemberDaySignEntity> memberDaySignEntities = new ArrayList<>();

    public SignDataPersistenceService(StringRedisTemplate redisTemplate, MemberDaySignService memberDaySignService, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.memberDaySignService = memberDaySignService;
        this.objectMapper = objectMapper;
    }

    private static final String SIGN_INFO_KEY = "sign:info:";
    private static final Pattern KEY_PATTERN = Pattern.compile("^sign:info:(\\d+)$");

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW,timeout = 300)
    public void persistLastMonthData() {
        // 扫描所有用户的签到键
        String pattern = SIGN_INFO_KEY + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) return;
        for (String key : keys) {
            processSignKey(key);
        }
        // 持久化到数据库
        memberDaySignService.saveBatch(memberDaySignEntities);
    }


    private void processSignKey(String key) {
        try {
            // 解析键信息
            KeyInfo keyInfo = parseKey(key);
            if (keyInfo == null) {
                return;
            }

            // 获取序列化的签到信息实体字符串
            String signInfoJson = redisTemplate.opsForValue().get(key);
            // 反序列化签到信息实体
            if (signInfoJson != null) {
                try {
                    MemberSignInfoEntity signInfo = objectMapper.readValue(signInfoJson, MemberSignInfoEntity.class);
                    String signStatusStr = bitsetToString(signInfo.getSignStatus());
                    // 转换为数据库实体
                    MemberDaySignEntity entity = convertToEntity(
                            keyInfo.memberId,
                            keyInfo.yearMonth,
                            signStatusStr
                    );
                    if (entity != null) {
                        this.memberDaySignEntities.add(entity);
                    }
                } catch (IOException e) {
                    // 反序列化失败
                    log.error("反序列化用户签到信息失败", e);
                }
            }
            else {
                log.warn("用户[{}]签到信息不存在", keyInfo.memberId);
            }

        } catch (Exception e) {
            // 记录错误但继续处理其他键
            log.error("处理键时发生错误， key: {}, Error: {}", key, e.getMessage());
        }
    }

    private KeyInfo parseKey(String key) {
        Matcher matcher = KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            log.warn("签到键{}格式非法", key);
            return null;
        }

        String memberId = matcher.group(1);
        // 获取上月年月信息
        String yearMonth = DateUtils.getLastMonthStr("yyyyMM");
        return new KeyInfo(Long.parseLong(memberId), yearMonth);
    }

    private MemberDaySignEntity convertToEntity(Long memberId, String yearMonth, String signStatusStr) {
        try {
            // 创建会员签到实体
            MemberDaySignEntity entity = new MemberDaySignEntity();
            entity.setMemberId(memberId);
            entity.setYearMonth(yearMonth);
            entity.setSignStatus(signStatusStr);
            log.info("正在创建签到数据实体，memberId: {}, yearMonth: {}", memberId, yearMonth);
            return entity;
        } catch (Exception e) {
            log.error("签到功能创建持久化实体失败，Error converting sign data for memberId: {}, yearMonth: {}", memberId, yearMonth, e);
            throw new RuntimeException("签到功能创建持久化实体失败，Failed to convert sign data", e);
        }
    }

    // 内部类用于存储解析后的键信息
    private static class KeyInfo {
        final Long memberId;
        final String yearMonth; // 格式 yyyyMM

        KeyInfo(Long memberId, String yearMonth) {
            this.memberId = memberId;
            this.yearMonth = yearMonth;
        }
    }

    private String bitsetToString(BitSet bitSet) {
        // 获取BitSet的字节数组
        byte[] bytes = bitSet.toByteArray();
        // 使用Base64编码
        return Base64.getEncoder().encodeToString(bytes);
    }

}

