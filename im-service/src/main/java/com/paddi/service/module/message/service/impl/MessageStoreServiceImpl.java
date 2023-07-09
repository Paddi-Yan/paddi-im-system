package com.paddi.service.module.message.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.model.message.*;
import com.paddi.service.module.message.mapper.GroupMessageHistoryMapper;
import com.paddi.service.module.message.mapper.MessageBodyMapper;
import com.paddi.service.module.message.mapper.MessageHistoryMapper;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.utils.SnowflakeIdWorker;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:53:00
 */
@Service
public class MessageStoreServiceImpl implements MessageStoreService {

    @Autowired
    private MessageHistoryMapper messageHistoryMapper;

    @Autowired
    private MessageBodyMapper messageBodyMapper;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private GroupMessageHistoryMapper groupMessageHistoryMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void storeMessage(MessageContent messageContent) {
        StoredMessageBody storedMessageBody = transformToMessageBody(messageContent);
        messageContent.setMessageKey(storedMessageBody.getMessageKey());
        DoStoreP2PMessageDTO doStoreP2PMessageDTO = new DoStoreP2PMessageDTO();
        doStoreP2PMessageDTO.setStoredMessageBody(storedMessageBody);
        doStoreP2PMessageDTO.setMessageContent(messageContent);
        rocketMQTemplate.convertAndSend(Constants.RocketMQConstants.STORE_P2P_MESSAGE, JSONObject.toJSONString(doStoreP2PMessageDTO));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void storeGroupMessage(GroupChatMessageContent groupChatMessageContent) {
        StoredMessageBody messageBody = transformToMessageBody(groupChatMessageContent);
        groupChatMessageContent.setMessageKey(messageBody.getMessageKey());
        DoStoreGroupMessageDTO doStoreGroupMessageDTO = new DoStoreGroupMessageDTO();
        doStoreGroupMessageDTO.setMessageContent(groupChatMessageContent);
        doStoreGroupMessageDTO.setStoredMessageBody(messageBody);
        rocketMQTemplate.convertAndSend(Constants.RocketMQConstants.STORE_GROUP_MESSAGE_GROUP, JSONObject.toJSONString(doStoreGroupMessageDTO));
    }

    @Override
    public <T> void setMessageToCache(MessageContent messageContent) {
        String cacheKey = messageContent.getAppId() +  Constants.RedisConstants.CACHE_MESSAGE  + messageContent.getMessageId();
        redisTemplate.opsForValue().set(cacheKey, JSONObject.toJSONString(messageContent), 5, TimeUnit.MINUTES);
    }

    @Override
    public <T> T getMessageFromCache(Integer appId, String messageId, Class<T> clazz) {
        String cacheKey = appId +  Constants.RedisConstants.CACHE_MESSAGE +  messageId;
        String str = redisTemplate.opsForValue().get(cacheKey);
        if(StrUtil.isEmpty(str)) {
            return null;
        }
        return JSONObject.parseObject(str, clazz);
    }

    private StoredMessageBody transformToMessageBody(MessageContent messageContent) {
        return StoredMessageBody.builder()
                                .appId(messageContent.getAppId())
                                .messageKey(snowflakeIdWorker.nextId())
                                .createTime(System.currentTimeMillis())
                                .securityKey("")
                                .extra(messageContent.getExtra())
                                .delFlag(DelFlagEnum.NORMAL.getCode())
                                .messageTime(messageContent.getMessageTime())
                                .messageBody(messageContent.getMessageBody())
                                .build();
    }
}
