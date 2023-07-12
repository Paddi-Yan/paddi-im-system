package com.paddi.service.module.message.service;

import com.paddi.common.model.Result;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.MessageReceiveACKContent;
import com.paddi.common.model.message.RecallMessageContent;
import com.paddi.common.model.message.SyncRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 17:48:05
 */
public interface MessageSyncService {
    void messageReceiveNotify(MessageReceiveACKContent messageReceiveACKContent);

    void messageReadNotify(MessageReadContent messageReadContent);

    void groupMessageReadNotify(MessageReadContent messageReadContent);

    Result syncOfflineMessage(SyncRequest request);

    void recallMessage(RecallMessageContent recallMessageContent);
}
