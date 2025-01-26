package com.hubbox.demo.config;

import java.util.concurrent.TimeUnit;

public record CacheSettings(
    Long expireAfterWrite,
    TimeUnit timeUnit,
    Long maximumSize,
    Boolean recordStats,
    Boolean weakKeys,
    Boolean softValues
) {
}
