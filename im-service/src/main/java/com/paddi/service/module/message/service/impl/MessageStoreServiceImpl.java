package com.paddi.service.module.message.service.impl;

import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.MessageContent;
import com.paddi.service.module.message.entity.GroupMessageHistory;
import com.paddi.service.module.message.entity.MessageBody;
import com.paddi.service.module.message.entity.MessageHistory;
import com.paddi.service.module.message.mapper.GroupMessageHistoryMapper;
import com.paddi.service.module.message.mapper.MessageBodyMapper;
import com.paddi.service.module.message.mapper.MessageHistoryMapper;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void storeMessage(MessageContent messageContent) {
        MessageBody messageBody = transformToMessageBody(messageContent);
        messageBodyMapper.insert(messageBody);
        List<MessageHistory> messageHistories = transformToMessageHistories(messageContent, messageBody);
        messageHistoryMapper.insertBatchSomeColumn(messageHistories);
        messageContent.setMessageKey(messageBody.getMessageKey());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void storeGroupMessage(GroupChatMessageContent groupChatMessageContent) {
        MessageBody messageBody = transformToMessageBody(groupChatMessageContent);
        messageBodyMapper.insert(messageBody);
        GroupMessageHistory groupMessageHistory = transformToGroupMessageHistory(groupChatMessageContent, messageBody);
        groupMessageHistoryMapper.insert(groupMessageHistory);
        groupChatMessageContent.setMessageKey(messageBody.getMessageKey());
    }

    private GroupMessageHistory transformToGroupMessageHistory(GroupChatMessageContent groupChatMessageContent,
                                                               MessageBody messageBody) {
        GroupMessageHistory groupMessageHistory = new GroupMessageHistory();
        BeanUtils.copyProperties(groupChatMessageContent, groupMessageHistory);
        groupMessageHistory.setGroupId(groupMessageHistory.getGroupId());
        groupMessageHistory.setMessageKey(messageBody.getMessageKey());
        groupMessageHistory.setCreateTime(System.currentTimeMillis());
        return groupMessageHistory;
    }

    private MessageBody transformToMessageBody(MessageContent messageContent) {
        return MessageBody.builder()
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

    private List<MessageHistory> transformToMessageHistories(MessageContent messageContent, MessageBody messageBody) {
        List<MessageHistory> messageHistories = new ArrayList<>();

        MessageHistory fromMessageHistory = new MessageHistory();
        BeanUtils.copyProperties(messageContent, fromMessageHistory);
        fromMessageHistory.setOwnerId(messageContent.getFromId());
        fromMessageHistory.setMessageKey(messageBody.getMessageKey());
        fromMessageHistory.setCreateTime(System.currentTimeMillis());

        MessageHistory toMessageHistory = new MessageHistory();
        BeanUtils.copyProperties(messageContent, toMessageHistory);
        toMessageHistory.setOwnerId(messageContent.getToId());
        toMessageHistory.setMessageKey(messageBody.getMessageKey());
        toMessageHistory.setCreateTime(System.currentTimeMillis());

        messageHistories.add(fromMessageHistory);
        messageHistories.add(toMessageHistory);
        return messageHistories;
    }
}
