package com.paddi.service.module.message.service;

import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.GroupOfflineMessageContent;
import com.paddi.common.model.message.MessageContent;
import com.paddi.common.model.message.OfflineMessageContent;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:52:45
 */
public interface MessageStoreService {

    void storeMessage(MessageContent messageContent);

    void storeGroupMessage(GroupChatMessageContent groupChatMessageContent);

    <T> void setMessageToCache(MessageContent messageContent);

    <T> T getMessageFromCache(Integer appId, String messageId, Class<T> clazz);

    void storeOfflineMessage(OfflineMessageContent offlineMessageContent);

    void storeGroupOfflineMessage(GroupOfflineMessageContent offlineMessageContent, List<String> groupMemberIdList);
}
