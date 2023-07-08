package com.paddi.service.module.message.service;

import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.MessageContent;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:52:45
 */
public interface MessageStoreService {

    void storeMessage(MessageContent messageContent);

    void storeGroupMessage(GroupChatMessageContent groupChatMessageContent);
}
