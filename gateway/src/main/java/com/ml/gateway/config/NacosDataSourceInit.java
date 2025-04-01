package com.ml.gateway.config;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.NacosFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
public class NacosDataSourceInit implements InitFunc {

    private static final String remoteAddress = "192.168.35.128:8848"; // **去掉 `/nacos/v1/cs/configs`**
    private static final String groupId = "dev";
    private static final String dataId = "sentinel.flow.rule";
    private static final String namespaceId = "48126882-0e93-4746-b265-ab232bf8d85e";

    @PostConstruct
    @Override
    public void init() {
        try {
            log.info("Initializing Nacos config service with address: " + remoteAddress);
            ConfigService configService = getConfigService();

            // **获取配置数据，显式传递 namespaceId**
            String flowRuleConfig = configService.getConfig(dataId, groupId, 5000);

            if (flowRuleConfig == null || flowRuleConfig.isEmpty()) {
                log.warn("Sentinel flow rule data is empty, config not loaded");
                return;
            }

            // **JSON 反序列化**
            List<FlowRule> flowRules = JSON.parseObject(flowRuleConfig, new TypeReference<List<FlowRule>>() {});

            // **注册规则**
            FlowRuleManager.loadRules(flowRules);
            log.info("Nacos flow rules initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize Nacos flow rules", e);
        }
    }

    private ConfigService getConfigService() throws Exception {
        // **正确方式：使用 Properties 传递参数**
        Properties properties = new Properties();
        properties.put("serverAddr", remoteAddress);
        properties.put("namespace", namespaceId); // **显式传递 namespaceId**

        return NacosFactory.createConfigService(properties);
    }

    public NacosDataSourceInit(){}
}
