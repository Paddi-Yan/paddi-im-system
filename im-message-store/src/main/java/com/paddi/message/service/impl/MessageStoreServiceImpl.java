package com.paddi.message.service.impl;

import com.paddi.common.model.message.*;
import com.paddi.message.entity.GroupMessageHistory;
import com.paddi.message.entity.MessageBody;
import com.paddi.message.entity.MessageHistory;
import com.paddi.message.mapper.GroupMessageHistoryMapper;
import com.paddi.message.mapper.MessageBodyMapper;
import com.paddi.message.mapper.MessageHistoryMapper;
import com.paddi.message.service.MessageStoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:38:20
 */
@Service
public class MessageStoreServiceImpl implements MessageStoreService {
    @Autowired
    private MessageHistoryMapper messageHistoryMapper;

    @Autowired
    private MessageBodyMapper messageBodyMapper;

    @Autowired
    private GroupMessageHistoryMapper groupMessageHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeMessage(DoStoreP2PMessageDTO doStoreP2PMessageDTO) {
        StoredMessageBody storedMessageBody = doStoreP2PMessageDTO.getStoredMessageBody();
        MessageBody messageBody = insertMessageBody(storedMessageBody);
        List<MessageHistory> messageHistories = transformToMessageHistories(doStoreP2PMessageDTO.getMessageContent(), messageBody);
        messageHistoryMapper.insertBatchSomeColumn(messageHistories);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeGroupMessage(DoStoreGroupMessageDTO doStoreGroupMessageDTO) {
        StoredMessageBody storedMessageBody = doStoreGroupMessageDTO.getStoredMessageBody();
        MessageBody messageBody = insertMessageBody(storedMessageBody);
        GroupMessageHistory groupMessageHistory = transformToGroupMessageHistory(doStoreGroupMessageDTO.getMessageContent(), messageBody);
        groupMessageHistoryMapper.insert(groupMessageHistory);
    }

    private MessageBody insertMessageBody(StoredMessageBody storedMessageBody) {
        MessageBody messageBody = new MessageBody();
        BeanUtils.copyProperties(storedMessageBody, messageBody);
        messageBodyMapper.insert(messageBody);
        return messageBody;
    }


    private List<MessageHistory> transformToMessageHistories(MessageContent messageContent, MessageBody messageBody) {
        List<MessageHistory> messageHistories = new ArrayList<>();

        MessageHistory fromMessageHistory = new MessageHistory();
        BeanUtils.copyProperties(messageContent, fromMessageHistory);
        fromMessageHistory.setOwnerId(messageContent.getFromId());
        fromMessageHistory.setMessageKey(messageBody.getMessageKey());
        fromMessageHistory.setCreateTime(System.currentTimeMillis());
        fromMessageHistory.setSequence(messageContent.getMessageSequence());

        MessageHistory toMessageHistory = new MessageHistory();
        BeanUtils.copyProperties(messageContent, toMessageHistory);
        toMessageHistory.setOwnerId(messageContent.getToId());
        toMessageHistory.setMessageKey(messageBody.getMessageKey());
        toMessageHistory.setCreateTime(System.currentTimeMillis());
        toMessageHistory.setSequence(messageContent.getMessageSequence());

        messageHistories.add(fromMessageHistory);
        messageHistories.add(toMessageHistory);
        return messageHistories;
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
}
