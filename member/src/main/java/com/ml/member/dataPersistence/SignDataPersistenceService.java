package com.ml.member.dataPersistence;

import com.ml.common.utils.DateUtils;
import com.ml.member.entity.MemberDaySignEntity;
import com.ml.member.service.MemberDaySignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
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

    public SignDataPersistenceService(
            StringRedisTemplate redisTemplate,
            MemberDaySignService memberDaySignService
    ) {
        this.redisTemplate = redisTemplate;
        this.memberDaySignService = memberDaySignService;
    }

    private static final String DAY_SIGN_LIST_KEY = "member:daySign:";
    private static final Pattern KEY_PATTERN = Pattern.compile("^member:sign:(\\d{6}):(\\d+)$");

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW,timeout = 300)
    public void persistLastMonthData() {
        String lastMonth = DateUtils.getLastMonthStr("yyyyMM");
        // 扫描上月所有用户的签到键
        String pattern = DAY_SIGN_LIST_KEY + lastMonth + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) return;

        // 使用管道批量处理
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                processSignKey(connection, key);
            }
            return null;
        });
    }


    private void processSignKey(RedisConnection connection, String key) {
        try {
            // 解析键信息
            KeyInfo keyInfo = parseKey(key);
            if (keyInfo == null) {
                return;
            }

            // 获取位图数据
            byte[] bitmapBytes = connection.get(key.getBytes());
            if (bitmapBytes == null || bitmapBytes.length == 0) {
                return;
            }

            // 转换为数据库实体
            List<MemberDaySignEntity> entities = convertToEntities(
                    keyInfo.memberId,
                    keyInfo.yearMonth,
                    bitmapBytes
            );

            // 批量保存到数据库
            if (!entities.isEmpty()) {
                memberDaySignService.saveBatch(entities);
            }

            // 删除Redis键(暂时不删除)
            //connection.del(key.getBytes());

        } catch (Exception e) {
            // 记录错误但继续处理其他键, 本应使用日志框架
            log.error("Error processing key: {}, Error: {}", key, e.getMessage());
        }
    }

    private KeyInfo parseKey(String key) {
        Matcher matcher = KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            return null;
        }

        String yearMonth = matcher.group(1);
        String memberId = matcher.group(2);

        return new KeyInfo(
                Long.parseLong(memberId),
                yearMonth
        );
    }

    private List<MemberDaySignEntity> convertToEntities(Long memberId, String yearMonth, byte[] bitmapBytes) {
        // 解析年月 (格式: yyyyMM -> YearMonth)
        YearMonth ym = YearMonth.of(
                Integer.parseInt(yearMonth.substring(0, 4)),
                Integer.parseInt(yearMonth.substring(4, 6))
        );

        // 获取当月天数
        int daysInMonth = ym.lengthOfMonth();
        BitSet bitSet = BitSet.valueOf(bitmapBytes);
        List<MemberDaySignEntity> entities = new ArrayList<>();

        // 遍历当月每一天
        for (int day = 0; day < daysInMonth; day++) {
            if (bitSet.get(day)) {
                entities.add(new MemberDaySignEntity(
                        memberId,
                        ym.atDay(day + 1).toString().replace("-",""), // 生成日期字符串 yyyyMMdd
                        1 // 签到状态
                ));
            }
        }
        return entities;
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

}

