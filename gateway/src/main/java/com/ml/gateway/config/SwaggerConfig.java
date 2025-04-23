package com.ml.gateway.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("member-service")
                .pathsToMatch("/api/member/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiGameApi() {
        return GroupedOpenApi.builder()
                .group("ai-game")
                .pathsToMatch("/api/ai/game/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiServiceApi() {
        return GroupedOpenApi.builder()
                .group("ai-service")
                .pathsToMatch("/api/ai/service/**")
                .build();
    }
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth-service")
                .pathsToMatch("/api/auth/**")
                .build();
    }
    @Bean
    public GroupedOpenApi blogApi() {
        return GroupedOpenApi.builder()
                .group("blog")
                .pathsToMatch("/api/blog/**")
                .build();
    }
    @Bean
    public GroupedOpenApi thirdPartyApi() {
        return GroupedOpenApi.builder()
                .group("third-party")
                .pathsToMatch("/api/third-party/**")
                .build();
    }

}
