package com.ml.blog.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HikariCPChecker implements CommandLineRunner {

    @Autowired
    private HikariDataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("HikariCP pool name: " + dataSource.getPoolName());
        log.info("HikariCP max pool size: " + dataSource.getMaximumPoolSize());
    }
}
