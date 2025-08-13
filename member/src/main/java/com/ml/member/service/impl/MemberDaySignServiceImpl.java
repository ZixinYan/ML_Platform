package com.ml.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.common.utils.DateUtils;
import com.ml.member.dao.DaySignGrowthAwardDao;
import com.ml.member.dao.GrowthChangeHistoryDao;
import com.ml.member.dao.MemberDao;
import com.ml.member.dao.MemberDaySignDao;
import com.ml.member.vo.MemberDaySignInfoRes;
import com.ml.member.entity.DaySignGrowthAwardEntity;
import com.ml.member.entity.MemberDaySignEntity;
import com.ml.member.entity.MemberEntity;
import com.ml.member.entity.MemberSignInfoEntity;
import com.ml.member.service.MemberDaySignService;
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

    private static final String SIGN_INFO_KEY = "sign:info:";
    
    @Autowired
    private GrowthChangeHistoryDao growthChangeHistoryDao;//暂时没写相关逻辑，下面有todo说明
    @Autowired
    private MemberDao memberDao ;
    @Autowired
    private DaySignGrowthAwardDao daySignGrowthAwardDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    
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

        // 获取或创建用户签到信息
        MemberSignInfoEntity signInfo = getMemberSignInfoEntity(memberId);
        // 检查今日是否已签到
        if (signInfo.getSignStatus().get(bitSetIndex)) {return false;}
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

        // 增加成长值（这一块因为属于用户成长值的，他不应该放在签到缓存里，所以暂时写和db交互）
        DaySignGrowthAwardEntity daySignGrowthAward = daySignGrowthAwardDao.selectOne(new QueryWrapper<DaySignGrowthAwardEntity>().eq("continue_day", signInfo.getContinueDays()));
        if (daySignGrowthAward != null) {
            MemberEntity member = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("member_id", memberId));
            member.setGrowth(member.getGrowth() + daySignGrowthAward.getGrowthAwardAmount());
            memberDao.updateById(member);
        }
        return true;
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
