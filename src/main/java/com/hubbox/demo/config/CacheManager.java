package com.hubbox.demo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.exceptions.CacheNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CacheManager {
    private final Map<String, Cache<?, ?>> caches;
    private final CacheConfig cacheConfig;

    @Inject
    private CacheManager(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        this.caches = new ConcurrentHashMap<>();
        initializeCaches();
    }


    private void initializeCaches() {
        if (cacheConfig == null || cacheConfig.caches() == null) {
            log.warn("No cache configuration found");
            return;
        }

        cacheConfig.caches().forEach(this::createCache);
    }

    private void createCache(String cacheName, CacheSettings settings) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // Apply cache settings
        if (settings.expireAfterWrite() != null && settings.timeUnit() != null) {
            builder.expireAfterWrite(settings.expireAfterWrite(), settings.timeUnit());
        }

        if (settings.maximumSize() != null) {
            builder.maximumSize(settings.maximumSize());
        }

        if (Boolean.TRUE.equals(settings.recordStats())) {
            builder.recordStats();
        }

        if (Boolean.TRUE.equals(settings.weakKeys())) {
            builder.weakKeys();
        }

        if (Boolean.TRUE.equals(settings.softValues())) {
            builder.softValues();
        }

        // Add cache removal listener

        Cache<?, ?> cache = builder.build();
        caches.put(cacheName, cache);
        log.info("Cache '{}' initialized with settings: {}", cacheName, settings);
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName) {
        Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException("Cache not found: " + cacheName);
        }
        return cache;
    }

    public void clearCache(String cacheName) {
        Cache<?, ?> cache = caches.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
            log.info("Cache '{}' cleared", cacheName);
        }
    }

    public void clearAllCaches() {
        caches.forEach((name, cache) -> {
            cache.invalidateAll();
            log.info("Cache '{}' cleared", name);
        });
    }

    public Map<String, CacheStats> getCacheStats() {
        return caches.entrySet().stream()
            .filter(entry -> cacheConfig.caches().get(entry.getKey()).recordStats())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stats()
            ));
    }
}
