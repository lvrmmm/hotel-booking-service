package ru.lvrmmm.hotelbookingservice.common.config.cache;

import org.redisson.api.RedissonClient;

import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.spring.cache.CacheConfig;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        config.put("rooms", new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}