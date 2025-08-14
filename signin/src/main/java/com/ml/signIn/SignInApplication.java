package com.ml.signIn;

import org.springframework.boot.SpringApplication;

@EnableRedisHttpSession
@MapperScan("com.ml.member.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages= "com.ml.signIn.feign")
@SpringBootApplication
@EnableScheduling
public class SignInApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}