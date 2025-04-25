package com.ml.aiservice.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.aiservice.feign.MemberFeignService;
import com.ml.aiservice.dto.IntegrationChangeHistoryDto;
import com.ml.common.utils.R;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.apache.http.client.utils.DateUtils.formatDate;

@Component
@Slf4j
public class Tools {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private ObjectMapper objectMapper;

    @Tool(name ="sum",value = "将两个参数a和b相加并返回结果")
    public double sum(
            @P(value = "加数1") double a,
            @P(value = "加数2") double b) {
        log.info("sum工具调用" + a + ":" + b);
        return a+b;
    }

    @Tool(name ="sqrt", value = "计算给定参数的平方根并返回结果")
    public double sqrt(
            @P(value = "平方根参数")double a) {
        log.info("sqrt工具调用" + a);
        return Math.sqrt(a);
    }

    @Tool(name = "get_user_integration_history", value = "根据提供的用户信息，查询用户的积分活动记录，并返回给用户")
    public String getRecord(
            @P(value = "用户ID") String userId,
            @P(value = "用户姓名") String userName) {
        log.info("查询积分工具调用" + userId + ":" + userName);
        try {
            // 调用远程服务获取积分变更记录
            R result = memberFeignService.getIntegration(Long.valueOf(userId));

            if (result.getCode() != 0) {
                return "查询失败：" + result.getMsg();
            }
            List<IntegrationChangeHistoryDto> list = objectMapper.convertValue(result.getData(), new TypeReference<List<IntegrationChangeHistoryDto>>() {});
            if (list == null) {
                return "没有找到用户 " + userName + " 的活动记录。";
            }

            StringBuilder sb = new StringBuilder("以下是用户【").append(userName).append("】的活动记录：\n");

            for (IntegrationChangeHistoryDto vo : list) {
                sb.append("- 来源：").append(sourceTypeToString(vo.getSourceType()))
                        .append("，积分变更：").append(vo.getChangeCount())
                        .append("，时间：").append(formatDate(vo.getCreateTime()))
                        .append("\n");
            }
            return  sb.toString();
        } catch (Exception e) {
            log.info("查询用户记录时发生错误：", e);
            return "查询用户记录时发生错误：" + e.getMessage();
        }
    }





    private String sourceTypeToString(Integer type) {
        return switch (type) {
            case 0 -> "游戏";
            case 1 -> "管理员修改";
            case 2 -> "活动";
            default -> "未知来源";
        };
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


}
