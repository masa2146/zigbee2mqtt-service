package com.hubbox.demo.config;

public record AppConfig(
    DatabaseConfig database,
    MqttConfig mqtt,
    CacheConfig cache,
    String contextPath
) {
}
