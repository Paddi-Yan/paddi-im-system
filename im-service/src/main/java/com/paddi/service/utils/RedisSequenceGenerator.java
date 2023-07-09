package com.paddi.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 19:03:29
 */
@Service
public class RedisSequenceGenerator {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Long generate(final String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
