package com.paddi.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.ConnectionStatusEnum;
import com.paddi.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 13:19:13
 */
@Component
public class UserSessionUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取用户所有终端的Session
     */
    public List<UserSession> getAllTerminalUserSession(Integer appId, String userId) {
        List<UserSession> userSessions = new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash()
                                                   .entries(appId + Constants.RedisConstants.USER_SESSION + userId);
        for(Object value : entries.values()) {
            UserSession userSession = JSONObject.parseObject((String) value, UserSession.class);
            if(userSession.getConnectionState().equals(ConnectionStatusEnum.ONLINE_STATUS.getCode())) {
                userSessions.add(userSession);
            }
        }
        return userSessions;
    }

    /**
     * 获取指定用户终端的Session
     */
    public UserSession getSpecifiedTerminalUserSession(Integer appId, String userId, Integer clientType, String imei) {
        String hashKey = appId + Constants.RedisConstants.USER_SESSION + userId;
        String fieldKey = buildTerminalIdentification(clientType, imei);
        UserSession userSession = JSONObject.parseObject((String) redisTemplate.opsForHash()
                                                                               .get(hashKey, fieldKey), UserSession.class);
        return userSession;
    }

    /**
     * 获取用户除了本终端以外的所有终端的Session
     */
    public List<UserSession> getOtherTerminalUserSession(Integer appId, String userId, Integer clientType, String imei) {
        List<UserSession> userSessions = new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash()
                                                   .entries(appId + Constants.RedisConstants.USER_SESSION + userId);
        //本终端的唯一标识
        String currentTerminal = buildTerminalIdentification(clientType, imei);
        for(Object value : entries.values()) {
            UserSession userSession = JSONObject.parseObject((String) value, UserSession.class);
            Boolean isTheSameTerminal = currentTerminal.equals(buildTerminalIdentification(userSession.getClientType(), userSession.getImei()));
            if(userSession.getConnectionState().equals(ConnectionStatusEnum.ONLINE_STATUS.getCode()) && !isTheSameTerminal) {
                userSessions.add(userSession);
            }
        }
        return userSessions;
    }

    private String buildTerminalIdentification(Integer clientType, String imei) {
        return clientType + ":" + imei;
    }
}
