package com.paddi.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.pack.user.UserStatusChangeNotifyPackage;
import com.paddi.codec.protocol.MessageHeader;
import com.paddi.common.enums.ConnectionStatusEnum;
import com.paddi.common.enums.command.UserEventCommand;
import com.paddi.common.model.UserClientDTO;
import com.paddi.common.model.UserSession;
import com.paddi.publish.MessageProducer;
import com.paddi.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.paddi.common.constants.Constants.*;
import static com.paddi.common.constants.Constants.RedisConstants.USER_SESSION;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:10:49
 */
public class SessionSocketHolder {

    private static final Map<UserClientDTO, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, String imei, NioSocketChannel channel) {
        UserClientDTO key = new UserClientDTO(appId, clientType, userId, imei);
        CHANNELS.put(key, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDTO key = new UserClientDTO(appId, clientType, userId, imei);
        return CHANNELS.get(key);
    }

    public static List<NioSocketChannel> get(Integer appId, String userId) {
        List<NioSocketChannel> channels = new ArrayList<>();
        for(Map.Entry<UserClientDTO, NioSocketChannel> entry : CHANNELS.entrySet()) {
            UserClientDTO key = entry.getKey();
            if(key.getAppId().equals(appId) && key.getUserId().equals(userId)) {
                channels.add(entry.getValue());
            }
        }
        return channels;
    }

    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDTO key = new UserClientDTO(appId, clientType, userId, imei);
        CHANNELS.remove(key);
    }

    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(entry -> entry.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    public static void removeUserSession(NioSocketChannel channel) {
        //删除Channel
        String userId  = (String) channel.attr(AttributeKey.valueOf(USERID)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(APPID)).get();
        Integer clientType = (Integer)channel.attr(AttributeKey.valueOf(CLIENT_TYPE)).get();
        String imei = (String) channel.attr(AttributeKey.valueOf(IMEI)).get();

        remove(appId, userId, clientType, imei);

        //删除Session
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        String sessionCacheKey = appId + USER_SESSION + userId;
        RMap<String, String> map = redissonClient.getMap(sessionCacheKey);
        map.remove(clientType + ":" + imei);

        sendUserOnlineStatusChangeMessageToLogicModule(userId, appId, clientType, imei);

        channel.close();
    }

    private static void sendUserOnlineStatusChangeMessageToLogicModule(String userId, Integer appId, Integer clientType, String imei) {
        MessageHeader messageHeader = MessageHeader.builder()
                                           .appId(appId)
                                           .imei(imei)
                                           .clientType(clientType)
                                           .build();
        UserStatusChangeNotifyPackage userStatusChangeNotifyPackage = new UserStatusChangeNotifyPackage();
        userStatusChangeNotifyPackage.setUserId(userId);
        userStatusChangeNotifyPackage.setAppId(appId);
        userStatusChangeNotifyPackage.setStatus(ConnectionStatusEnum.OFFLINE_STATUS.getCode());
        MessageProducer.sendMessage(userStatusChangeNotifyPackage, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
    }

    public static void offlineUserSession(NioSocketChannel channel) {
        String userId  = (String) channel.attr(AttributeKey.valueOf(USERID)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(APPID)).get();
        Integer clientType = (Integer)channel.attr(AttributeKey.valueOf(CLIENT_TYPE)).get();
        String imei = (String) channel.attr(AttributeKey.valueOf(IMEI)).get();

        remove(appId, userId, clientType, imei);

        //更新Session状态为离线
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        String sessionCacheKey = appId + USER_SESSION + userId;
        RMap<String, String> map = redissonClient.getMap(sessionCacheKey);
        String fieldKey = clientType + ":" + imei;
        String sessionStr = map.get(fieldKey);
        if(StrUtil.isNotEmpty(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectionState(ConnectionStatusEnum.OFFLINE_STATUS.getCode());
            map.put(fieldKey, JSON.toJSONString(userSession));
        }

        sendUserOnlineStatusChangeMessageToLogicModule(userId, appId, clientType, imei);

    }

}
