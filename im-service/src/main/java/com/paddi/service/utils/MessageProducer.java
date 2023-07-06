package com.paddi.service.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.protocol.MessagePackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.Command;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 13:47:16
 */
@Component
@Slf4j
public class MessageProducer {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private UserSessionUtils sessionUtils;

    private static final String TOPIC = Constants.RocketMQConstants.MessageService2Im + "-";

    private Boolean sendMessage(UserSession userSession, Object payload){
        try {
            log.info("send message = [{}]", payload);
            rocketMQTemplate.convertAndSend(TOPIC + userSession.getBrokerId(), payload);
            return true;
        }catch(Exception e) {
            log.error("send message {} error {}", payload, e.getMessage());
            return false;
        }
    }

    /**
     * 封装数据之后调用sendMessage
     */
    private Boolean sendPackage(String toId, Command command, Object message, UserSession userSession) {
        MessagePackage messagePackage = MessagePackage.builder()
                                                      .command(command.getCommand())
                                                      .appId(userSession.getAppId())
                                                      .toId(toId)
                                                      .clientType(userSession.getClientType())
                                                      .imei(userSession.getImei()).build();
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(message));
        messagePackage.setData(jsonObject);
        String payload = JSONObject.toJSONString(messagePackage);
        return sendMessage(userSession, payload);
    }

    /**
     * 当clientType或imei为空发送给所有终端
     * 当clientType和imei不等于空 发送给除了此终端以外的所有终端
     */
    public void sendToUser(String toId, Integer appId, Integer clientType, String imei, Command command, Object message) {
        if(clientType != null && StrUtil.isNotEmpty(imei)) {
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToOtherUserTerminal(toId, command, message, clientInfo);
        }else {
            sendToAllUserTerminal(toId, command, message, appId);
        }
    }


    /**
     * 发送给所有终端
     */
    public List<ClientInfo> sendToAllUserTerminal(String toId, Command command, Object message, Integer appId) {
        List<UserSession> allTerminalUserSession = sessionUtils.getAllTerminalUserSession(appId, toId);
        List<ClientInfo> result = new ArrayList<>();
        for(UserSession userSession : allTerminalUserSession) {
            Boolean success = sendPackage(toId, command, message, userSession);
            if(success) {
                result.add(new ClientInfo(appId, userSession.getClientType(), userSession.getImei()));
            }
        }
        return result;
    }

    /**
     * 发送给指定某一终端
     */
    public void sendToSpecifiedUserTerminal(String toId, Command command, Object message, ClientInfo clientInfo) {
        UserSession specifiedTerminalUserSession = sessionUtils.getSpecifiedTerminalUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());
        sendPackage(toId, command, message, specifiedTerminalUserSession);
    }

    /**
     * 发送给除了当前终端以外的所有终端
     */
    public void sendToOtherUserTerminal(String toId, Command command, Object message, ClientInfo clientInfo) {
        List<UserSession> otherTerminalUserSession = sessionUtils.getOtherTerminalUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());
        for(UserSession userSession : otherTerminalUserSession) {
            sendPackage(toId, command, message, userSession);
        }
    }
}
