package com.truckoptimization.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "api")
@Data
public class APIConfig {
    private boolean useLocalMatrixCalculation;
}
