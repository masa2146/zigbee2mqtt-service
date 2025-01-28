package com.hubbox.demo.config;

public record DatabaseConfig(
    String url,
    String username,
    String password
) {
}
