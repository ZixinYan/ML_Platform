package com.ml.signIn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.common.utils.DateUtils;
import com.ml.common.utils.RedisLock;
import com.ml.signIn.dao.DaySignGrowthAwardDao;
import com.ml.signIn.dao.MemberDaySignDao;
import com.ml.signIn.feign.MemberFeignService;
import com.ml.signIn.vo.MemberDaySignInfoRes;
import com.ml.signIn.entity.DaySignGrowthAwardEntity;
import com.ml.signIn.entity.MemberDaySignEntity;
import com.ml.signIn.entity.MemberSignInfoEntity;
import com.ml.signIn.service.MemberDaySignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

@Slf4j
@Service("memberDaySignService")
public class MemberDaySignServiceImpl extends ServiceImpl<MemberDaySignDao, MemberDaySignEntity> implements MemberDaySignService {

    private static final String SIGN_INFO_KEY = "sign:info:";//redis缓存签到信息key前缀
    private static final long expireTime = 30000; // 锁过期时间（30秒）
    private static final long waitTime = 5000; // 等待获取锁的最大时间（5秒）

    @Autowired
    private MemberFeignService memberFeignClient;
    @Autowired
    private DaySignGrowthAwardDao daySignGrowthAwardDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisLock redisLock;
    
    private String getSignInfoKey(Long memberId) {
        return SIGN_INFO_KEY + memberId;
    }

    /**
     * 用户签到(基于redis)
     * @param memberId
     * @return true 签到成功，false 已签到过
     */
    @Override
    public Boolean daySignIn(Long memberId) {
        String todayDate = DateUtils.getTodayStr();
        int bitSetIndex = DateUtils.getDayOfMonth(todayDate) - 1;
        //创建单用户粒度锁键
        String lockKey = "lock:member:sign:" + memberId;
        String lockValue = UUID.randomUUID().toString(); // 唯一标识，避免释放其他线程的锁

        try {
            boolean locked = redisLock.tryLock(lockKey, lockValue, expireTime, waitTime);
            if (!locked) {
                log.warn("用户签到失败，获取锁超时: memberId={}", memberId);
                return false; // 获取锁失败，返回签到失败
            }
            // 获取或创建用户签到信息
            MemberSignInfoEntity signInfo = getMemberSignInfoEntity(memberId);
            // 检查今日是否已签到
            if (signInfo.getSignStatus().get(bitSetIndex)) {
                return false;
            }
            // 更新签到状态、连续签到信息、最后签到日期
            signInfo.getSignStatus().set(bitSetIndex, true);
            if (DateUtils.getYesterdayStr().equals(signInfo.getLastSignDate())) {
                signInfo.setContinueDays(signInfo.getContinueDays() + 1);
            } else {
                signInfo.setContinueDays(1);
                signInfo.setStartSignDate(todayDate);
            }
            signInfo.setLastSignDate(todayDate);

            // 保存签到信息到Redis
            saveMemberSignInfo(memberId, signInfo);

            //利用feign调用member的update接口修改成长值，直接和db交互
            Map <String , Object> updatemap= new HashMap<>();
            updatemap.put("id", memberId);
            DaySignGrowthAwardEntity daySignGrowthAward = daySignGrowthAwardDao.selectOne(new QueryWrapper<DaySignGrowthAwardEntity>().eq("continue_day", signInfo.getContinueDays()));
            if (daySignGrowthAward != null) {
                updatemap.put("growth",daySignGrowthAward.getGrowthAwardAmount());
            }else{
                log.error("签到奖励表配置有误");
            }
            memberFeignClient.update(updatemap);

            return true;

        } finally {
            redisTemplate.delete(lockKey);// 释放锁
        }
    }

    /**
     * 获取用户签到信息
     */
    private MemberSignInfoEntity getMemberSignInfoEntity(Long memberId) {
        String signInfoKey = getSignInfoKey(memberId);
        String signInfoJson = redisTemplate.opsForValue().get(signInfoKey);
        if (signInfoJson != null) {
            try {
                return objectMapper.readValue(signInfoJson, MemberSignInfoEntity.class);
            } catch (IOException e) {
                // 反序列化失败
                log.error("反序列化用户[{}]签到信息失败",memberId, e);
            }
        }
        // 如果不存在或反序列化失败，返回新的签到信息对象
        return new MemberSignInfoEntity();
    }

    /**
     * 保存用户签到信息到Redis
     */
    private void saveMemberSignInfo(Long memberId, MemberSignInfoEntity signInfo) {
        String signInfoKey = getSignInfoKey(memberId);
        try {
            String signInfoJson = objectMapper.writeValueAsString(signInfo);
            redisTemplate.opsForValue().set(signInfoKey, signInfoJson, 40, TimeUnit.DAYS); // 40天过期
        } catch (IOException e) {
            log.error("序列化用户[{}]签到信息失败",memberId, e);
        }
    }

    /**
     * 查询用户连续签到信息（签到日历）(基于redis)
     * @param memberId
     * @return
     */
    @Override
    public MemberDaySignInfoRes daySignInfo(Long memberId) {
        MemberDaySignInfoRes memberDaySignInfoRes = new MemberDaySignInfoRes();

        // 获取用户签到信息
        MemberSignInfoEntity signInfo = getMemberSignInfoEntity(memberId);
        // 构建日历数据
        String signStatusStr = bitsetToString(signInfo.getSignStatus());

        // 封装返回结果
        memberDaySignInfoRes.setStartSingDate(signInfo.getStartSignDate());
        memberDaySignInfoRes.setContinueDay(signInfo.getContinueDays());
        memberDaySignInfoRes.setSignStatusStr(signStatusStr);
        return memberDaySignInfoRes;
    }

    /**
     * 将BitSet转换为Base64字符串
     */
    private String bitsetToString(BitSet bitSet) {
        // 获取BitSet的字节数组
        byte[] bytes = bitSet.toByteArray();
        // 使用Base64编码
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将Base64字符串转回BitSet
     * （此方法可在前端或需要解析的地方实现）
     */
    private BitSet stringToBitset(String str) {
        byte[] bytes = Base64.getDecoder().decode(str);
        return BitSet.valueOf(bytes);
    }

}
