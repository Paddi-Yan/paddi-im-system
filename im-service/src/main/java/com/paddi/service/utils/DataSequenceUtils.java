package com.paddi.service.utils;

import com.paddi.common.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 15:57:48
 */
@Component
public class DataSequenceUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 使用Redis中的Hash结构来存储
     * hashKey: appId:sequence:userId
     * field-value: friend: 10
     *              group: 20
     *              conversation: 19
     * @param appId
     */
    public void writeSequence(Integer appId, String userId, String type, Long sequence) {
        String hashKey = appId + Constants.RedisConstants.SEQUENCE_PREFIX + userId;
        redisTemplate.opsForHash().put(hashKey, type, String.valueOf(sequence));
    }
}
