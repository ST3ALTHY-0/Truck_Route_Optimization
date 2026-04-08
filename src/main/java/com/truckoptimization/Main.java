package com.truckoptimization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = {
    "com.truckoptimization.task",
    "com.truckoptimization.common",
    "com.truckoptimization.dto",
    "com.truckoptimization.exception"
})
@EntityScan(basePackages = {
    "com.truckoptimization.dto"
})
@EnableJpaRepositories(basePackages = {
    "com.truckoptimization.dto"
})
@EnableCaching
public class Main {


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }


}

