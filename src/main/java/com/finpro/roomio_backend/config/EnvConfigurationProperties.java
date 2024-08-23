package com.finpro.roomio_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public record EnvConfigurationProperties(String env) {
}