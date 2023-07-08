package com.paddi.service.module.message.service;

import com.paddi.common.model.message.MessageContent;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:02:54
 */
public interface P2PMessageService {
    void process(MessageContent messageContent);
}
