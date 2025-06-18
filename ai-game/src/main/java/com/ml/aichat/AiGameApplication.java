package com.ml.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRedisHttpSession(redisNamespace = "spring:session")
@EnableFeignClients
public class AiGameApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiGameApplication.class, args);
    }
}
