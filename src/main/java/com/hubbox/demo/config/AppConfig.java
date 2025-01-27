package com.hubbox.demo.config;

public record AppConfig(
    MqttConfig mqtt,
    CacheConfig cache,
    String contextPath
) {
}
