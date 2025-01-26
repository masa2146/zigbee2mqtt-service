package com.hubbox.demo.config;


import java.util.Map;

public record CacheConfig(
    Map<String, CacheSettings> caches
) {
}
