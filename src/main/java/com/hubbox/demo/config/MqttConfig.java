package com.hubbox.demo.config;

public record MqttConfig(
    String host,
    int port,
    String id,
    String username,
    String password,
    String topicFilter
) {

}
