package com.ml.signIn;

import com.ml.member.MemberApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@MapperScan("com.ml.signIn.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages= "com.ml.signIn.feign")
@SpringBootApplication
@EnableScheduling
public class SignInApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}