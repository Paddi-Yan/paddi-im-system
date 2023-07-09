package com.paddi.service.module.message.service.impl;

import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.message.MessageReceiveACKContent;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 17:48:12
 */
@Service
public class MessageSyncServiceImpl implements MessageSyncService {

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void messageReceiveNotify(MessageReceiveACKContent messageReceiveACKContent) {
        messageProducer.sendToAllUserTerminal(messageReceiveACKContent.getToId(), MessageCommand.MSG_RECEIVE_ACK,
                messageReceiveACKContent, messageReceiveACKContent.getAppId());
    }
}
