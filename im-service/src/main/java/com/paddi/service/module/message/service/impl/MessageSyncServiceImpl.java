package com.paddi.service.module.message.service.impl;

import com.paddi.codec.pack.message.MessageReadPackage;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.MessageReceiveACKContent;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
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

    /**
     * 已读消息的流程
     * 1. 更新会话的Sequence
     * 2. 通知在线的同步端
     * 3. 发送已读回执给对方
     * @param messageReadContent
     */
    @Override
    public void messageReadNotify(MessageReadContent messageReadContent) {
        //TODO 更新会话的Sequence
        MessageReadPackage messageReadPackage = new MessageReadPackage();
        BeanUtils.copyProperties(messageReadContent, messageReadPackage);
        //通知在线的同步端
        syncToOperator(messageReadPackage, messageReadContent);
        //发送已读回执
        messageProducer.sendToAllUserTerminal(messageReadContent.getToId(), MessageCommand.MSG_READ_RECEIPT,
                messageReadPackage, messageReadContent.getAppId());
    }

    private void syncToOperator(MessageReadPackage messageReadPackage, MessageReadContent messageReadContent) {
        messageProducer.sendToOtherUserTerminal(messageReadContent.getFromId(),
                MessageCommand.MSG_READ_NOTIFY,
                messageReadPackage,
                new ClientInfo(messageReadContent.getAppId(),
                        messageReadContent.getClientType(),
                        messageReadContent.getImei()));
    }
}
