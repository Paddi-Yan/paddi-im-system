package com.paddi.service.module.message.service;

import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.MessageReceiveACKContent;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 17:48:05
 */
public interface MessageSyncService {
    void messageReceiveNotify(MessageReceiveACKContent messageReceiveACKContent);

    void messageReadNotify(MessageReadContent messageReadContent);
}
