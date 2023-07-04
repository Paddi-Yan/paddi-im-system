package com.paddi.redis;

import cn.hutool.core.util.StrUtil;
import com.paddi.config.BootstrapConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:40:13
 */
public class SingleRedisClientStrategy {

    public static final String REDIS_ADDRESS_PREFIX = "redis://";

    public RedissonClient getRedissonClient(BootstrapConfig.RedisConfig redisConfig) {
        Config config = new Config();
        String address = redisConfig.getSingle().getAddress();
        address = address.startsWith(REDIS_ADDRESS_PREFIX) ? address : REDIS_ADDRESS_PREFIX + address;
        SingleServerConfig serverConfig = config.useSingleServer()
                                                      .setAddress(address)
                                                      .setDatabase(redisConfig.getDatabase())
                                                      .setTimeout(redisConfig.getTimeout())
                                                      .setConnectionMinimumIdleSize(redisConfig.getPoolMinIdle())
                                                      .setConnectTimeout(redisConfig.getPoolConnTimeout())
                                                      .setConnectionPoolSize(redisConfig.getPoolSize());
        if(StrUtil.isNotBlank(redisConfig.getPassword())) {
            serverConfig.setPassword(redisConfig.getPassword());
        }
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }
}
