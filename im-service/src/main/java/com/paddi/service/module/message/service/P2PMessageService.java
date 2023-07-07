package com.paddi.service.module.message.service;

import com.paddi.service.module.message.model.MessageContent;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:02:54
 */
public interface P2PMessageService {
    void process(MessageContent messageContent);
}
