package com.paddi.redis;

import com.paddi.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:39:23
 */
public class RedisManager {

    private static RedissonClient client;

    public static void init(BootstrapConfig.RedisConfig config) {
        SingleRedisClientStrategy redisClientStrategy = new SingleRedisClientStrategy();
        client = redisClientStrategy.getRedissonClient(config);
    }

    public static RedissonClient getRedissonClient() {
        return client;
    }
}
