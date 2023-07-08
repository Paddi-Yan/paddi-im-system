package com.paddi.service.module.message.service;


import com.paddi.common.model.message.GroupChatMessageContent;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月08日 23:17:38
 */
public interface GroupMessageService {


    void process(GroupChatMessageContent groupChatMessageContent);
}
