package com.paddi.service.module.message.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.model.message.*;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.conversation.service.ConversationService;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.utils.SnowflakeIdWorker;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:53:00
 */
@Service
public class MessageStoreServiceImpl implements MessageStoreService {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private ApplicationConfiguration configuration;

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

    @Override
    public void storeOfflineMessage(OfflineMessageContent offlineMessageContent) {
        String fromKey = offlineMessageContent.getAppId() + Constants.RedisConstants.OFFLINE_MESSAGE + offlineMessageContent.getFromId();
        String toKey = offlineMessageContent.getAppId() + Constants.RedisConstants.OFFLINE_MESSAGE + offlineMessageContent.getToId();
        ZSetOperations<String, String> operations = redisTemplate.opsForZSet();
        //判断队列中的离线消息是否超过限定值
        if(operations.zCard(fromKey) > configuration.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessageContent.setConversationId(
                conversationService.generateConversationId(
                        offlineMessageContent.getConversationType(),
                        offlineMessageContent.getFromId(),
                        offlineMessageContent.getToId())
        );
        operations.add(fromKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());
        if(operations.zCard(toKey) > configuration.getOfflineMessageCount()) {
            operations.removeRange(toKey, 0, 0);
        }
        offlineMessageContent.setConversationId(
                conversationService.generateConversationId(
                        offlineMessageContent.getConversationType(),
                        offlineMessageContent.getToId(),
                        offlineMessageContent.getFromId())
        );
        operations.add(toKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());

    }

    @Override
    public void storeGroupOfflineMessage(GroupOfflineMessageContent offlineMessageContent,
                                         List<String> groupMemberIdList) {
        ZSetOperations<String, String> operations = redisTemplate.opsForZSet();
        for(String memberId : groupMemberIdList) {
            String memberKey = offlineMessageContent.getAppId() + Constants.RedisConstants.OFFLINE_MESSAGE + memberId;
            //判断队列中的离线消息是否超过限定值
            if(operations.zCard(memberKey) > configuration.getOfflineMessageCount()) {
                operations.removeRange(memberKey, 0, 0);
            }
            offlineMessageContent.setConversationId(
                    conversationService.generateConversationId(
                            offlineMessageContent.getConversationType(),
                            offlineMessageContent.getFromId(),
                            offlineMessageContent.getGroupId())
            );
            operations.add(memberKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());
        }
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
