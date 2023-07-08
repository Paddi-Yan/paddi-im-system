package com.paddi.service.module.message.service.impl;

import com.paddi.codec.pack.message.ChatMessageACK;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.MessageContent;
import com.paddi.service.module.message.service.CheckSendMessageService;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.module.message.service.P2PMessageService;
import com.paddi.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:03:22
 */
@Service
@Slf4j
public class P2PMessageServiceImpl implements P2PMessageService {

    @Autowired
    private CheckSendMessageService checkSendMessageService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private MessageStoreService messageStoreService;

    private final ThreadPoolExecutor threadPoolExecutor;{
        AtomicInteger counter = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1024), factory -> {
            Thread thread = new Thread();
            thread.setName("message-process-thread-" + counter.decrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void process(MessageContent messageContent) {
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //是否被禁用/是否被禁言
        //发送方和接收方是否是好友
        Result result = processBefore(fromId, toId, appId);
        if(result.isSuccess()) {
            threadPoolExecutor.execute(() -> {
                //数据持久化
                messageStoreService.storeMessage(messageContent);
                //回复ACK给发送方
                sendACK(messageContent, result);
                //同步给发送方的其他在线端
                syncToSender(messageContent, new ClientInfo(appId, messageContent.getClientType(), messageContent.getImei()));
                //发送给对方在线端
                dispatchMessage(messageContent);
            });
        }else {
            //回复ACK给发送方,通知发送失败
            sendACK(messageContent, result);
        }
    }

    private Result processBefore(String fromId, String toId, Integer appId) {
        Result result = checkSendMessageService.checkSenderForbidOrMute(fromId, appId);
        if(!result.isSuccess()) {
            return result;
        }
        result = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        if(!result.isSuccess()) {
            return result;
        }
        return Result.success();
    }

    private void sendACK(MessageContent messageContent, Result result) {
        log.info("Message Ack: messageId = [{}], checkResult = [{}]", messageContent.getMessageId(), result);
        ChatMessageACK chatMessageACK = new ChatMessageACK(messageContent.getMessageId());
        result.setData(chatMessageACK);
        messageProducer.sendToSpecifiedUserTerminal(messageContent.getFromId(), MessageCommand.MSG_ACK,
                result, messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToOtherUserTerminal(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private void dispatchMessage(MessageContent dispatchedMessage) {
        messageProducer.sendToAllUserTerminal(dispatchedMessage.getToId(), MessageCommand.MSG_P2P, dispatchedMessage, dispatchedMessage.getAppId());
    }
}
