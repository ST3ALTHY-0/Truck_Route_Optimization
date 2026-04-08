package com.truckoptimization.common.config;

import org.springframework.context.annotation.Configuration;

import com.google.ortools.Loader;

import jakarta.annotation.PostConstruct;

@Configuration
public class ORToolsConfig {

    @PostConstruct
    public void loadORTools() {
        Loader.loadNativeLibraries();
    }
}
