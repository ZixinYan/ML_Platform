package com.ml.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean
    public Scheduler assistantScheduler() {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(10));
    }
}
