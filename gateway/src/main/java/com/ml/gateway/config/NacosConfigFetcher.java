package com.ml.gateway.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NacosConfigFetcher {

    private final NacosDataSourceInit nacosDataSourceInit;

    public NacosConfigFetcher(NacosDataSourceInit nacosDataSourceInit) {
        this.nacosDataSourceInit = nacosDataSourceInit;
    }

    // 每 5 分钟拉取一次 Nacos 配置
    @Scheduled(fixedRate = 300000)
    public void fetchConfig() {
        nacosDataSourceInit.init();
    }
}
