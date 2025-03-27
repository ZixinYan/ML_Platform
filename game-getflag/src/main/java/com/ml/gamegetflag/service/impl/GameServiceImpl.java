package com.ml.gamegetflag.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.common.utils.R;
import com.ml.gamegetflag.feign.MemberFeignService;
import com.ml.gamegetflag.service.GameService;
import com.ml.gamegetflag.vo.GrowthChangeHistoryVo;
import com.ml.gamegetflag.vo.IntegrationChangeHistoryVo;
import com.ml.gamegetflag.vo.MemberVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

    @Autowired
    private KafkaProducerServiceImpl kafkaProducerServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${game.integration}")
    private int integration;

    @Value(("${game.growth}"))
    private int growth;

    @Override
    public boolean generate(String prompt,String requestId) {
        try {
            // 组织 Kafka 发送的消息
            Map<String, String> message = new HashMap<>();
            message.put("request_id", requestId);
            message.put("prompt", prompt);
            // 发送消息到 Kafka
            kafkaProducerServiceImpl.sendMessage(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean success(String requestId, String flag, Long memberId) {
        String realFlag = redisTemplate.opsForValue().get(requestId);
        if (realFlag == null) {
            log.info("会话错误");
            return false;
        }

        if (!realFlag.equals(flag)) {
            log.warn("Flag 验证失败！");
            return false;
        }
        R r = memberFeignService.getMemberById(memberId);
        if (r == null || r.getCode() != 0) {
            log.error("远程调用失败，无法获取用户信息: memberId={}", memberId);
            return false;
        }
        MemberVo memberVo = objectMapper.convertValue(r.getData(), MemberVo.class);
        if (memberVo == null) {
            log.error("用户不存在: requestId={}, memberId={}", requestId, memberId);
            return false;
        }

        // 记录积分变更历史
        if (!updateMemberInfo(memberVo, integration, growth)) {
            return false;
        }

        // 记录成长值变更历史
        if (!saveGrowthHistory(memberId, growth)) {
            return false;
        }

        // 更新用户的成长值和积分（累加）
        return saveIntegrationHistory(memberId, integration);
    }

    /**
     * 记录积分变更历史
     */
    private boolean saveIntegrationHistory(Long memberId, int integration) {
        IntegrationChangeHistoryVo vo = new IntegrationChangeHistoryVo();
        vo.setNote("游戏积分奖励");
        vo.setChangeCount(integration);
        vo.setMemberId(memberId);
        vo.setSourceTyoe(0);
        vo.setCreateTime(new Date());

        R r = memberFeignService.saveIntegration(vo);
        if (r.getCode() != 0) {
            log.error("积分记录失败: memberId={}", memberId);
            return false;
        }
        log.info("积分记录成功: +{}", integration);
        return true;
    }

    /**
     * 记录成长值变更历史
     */
    private boolean saveGrowthHistory(Long memberId, int growth) {
        GrowthChangeHistoryVo vo = new GrowthChangeHistoryVo();
        vo.setMemberId(memberId);
        vo.setChangeCount(growth);
        vo.setCreateTime(new Date());
        vo.setNote("游戏成长值奖励");
        vo.setSourceType(0);

        R r = memberFeignService.saveGrowth(vo);
        if (r.getCode() != 0) {
            log.error("成长值记录失败: memberId={}", memberId);
            return false;
        }
        log.info("成长值记录成功: +{}", growth);
        return true;
    }

    /**
     * 更新用户信息，累加成长值和积分
     */
    private boolean updateMemberInfo(MemberVo member, int integrationAdd, int growthAdd) {
        MemberVo updateVo = new MemberVo();
        updateVo.setId(member.getId());
        updateVo.setIntegration(member.getIntegration() + integrationAdd);
        updateVo.setGrowth(member.getGrowth() + growthAdd);

        R updateResponse = memberFeignService.updateMember(updateVo);
        if (updateResponse.getCode() != 0) {
            log.error("用户信息更新失败: {}", member.getId());
            return false;
        }

        log.info("用户 {} 更新成功: 积分+{}, 成长值+{}", member.getId(), integrationAdd, growthAdd);
        return true;
    }

}
