package com.paddi.service.module.message.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.paddi.codec.pack.message.ChatMessageACK;
import com.paddi.codec.pack.message.MessageReceiveServerACKPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.MessageContent;
import com.paddi.service.module.message.service.CheckSendMessageService;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.module.message.service.P2PMessageService;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;

    private final ThreadPoolExecutor threadPoolExecutor;{
        AtomicInteger counter = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1024), runnable -> {
                    Thread thread = new Thread(runnable);
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

        //从缓存中获取消息
        MessageContent messageFromCache = messageStoreService.getMessageFromCache(messageContent.getAppId(), messageContent.getMessageId(), MessageContent.class);
        if(messageFromCache != null) {
            //不需要进行持久化 直接分发消息
            threadPoolExecutor.execute(() -> {
                //回复ACK给发送方
                sendACK(messageFromCache, Result.success());
                //同步给发送方的其他在线端
                syncToSender(messageFromCache, new ClientInfo(appId, messageFromCache.getClientType(), messageFromCache.getImei()));
                //发送给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromCache);
                if(CollectionUtil.isEmpty(clientInfos)) {
                    //接收方没有在线客户端的情况下由服务端向发送方发送ACK
                    sendServerReceiveACK(messageFromCache);
                }
            });
            return;
        }

        //sequenceKey: appId + Seq + (from + to) / group
        String key = appId + Constants.SequenceConstants.Message + formalize(messageContent.getFromId(), messageContent.getToId());
        Long sequence = sequenceGenerator.generate(key);
        messageContent.setMessageSequence(sequence);

        //前置校验-已迁移至TCP网关层完成校验
        //是否被禁用/是否被禁言
        //发送方和接收方是否是好友
        threadPoolExecutor.execute(() -> {
            //数据持久化
            messageStoreService.storeMessage(messageContent);
            //回复ACK给发送方
            sendACK(messageContent, Result.success());
            //同步给发送方的其他在线端
            syncToSender(messageContent, new ClientInfo(appId, messageContent.getClientType(), messageContent.getImei()));
            //发送给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            if(CollectionUtil.isEmpty(clientInfos)) {
                //接收方没有在线客户端的情况下由服务端向发送方发送ACK
                sendServerReceiveACK(messageContent);
            }
            //将消息存入到缓存中
            messageStoreService.setMessageToCache(messageContent);
        });
    }

    private String formalize(String fromId, String toId) {
        if(fromId.compareTo(toId) < 0) {
            return fromId + "-" + toId;
        }else if(fromId.compareTo(toId) > 0){
            return toId + "-" + fromId;
        }
        throw new RuntimeException("格式无法统一");
    }


    private void sendServerReceiveACK(MessageContent messageContent) {
        MessageReceiveServerACKPackage ackPackage = new MessageReceiveServerACKPackage();
        ackPackage.setFromId(messageContent.getToId());
        ackPackage.setToId(messageContent.getFromId());
        ackPackage.setMessageKey(messageContent.getMessageKey());
        ackPackage.setSendFromServer(true);
        ackPackage.setMessageSequence(messageContent.getMessageSequence());
        messageProducer.sendToSpecifiedUserTerminal(messageContent.getFromId(),
                MessageCommand.MSG_RECEIVE_ACK,
                ackPackage,
                new ClientInfo(messageContent.getAppId(),
                        messageContent.getClientType(),
                        messageContent.getImei()));
    }

    @Override
    public Result processBefore(String fromId, String toId, Integer appId) {
        Result result = checkSendMessageService.checkSenderForbidOrMute(fromId, appId);
        if(!result.isSuccess()) {
            return result;
        }
        result = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return result;
    }

    private void sendACK(MessageContent messageContent, Result result) {
        log.info("Message Ack: messageId = [{}], checkResult = [{}]", messageContent.getMessageId(), result);
        ChatMessageACK chatMessageACK = new ChatMessageACK(messageContent.getMessageId(), messageContent.getMessageSequence());
        result.setData(chatMessageACK);
        messageProducer.sendToSpecifiedUserTerminal(messageContent.getFromId(), MessageCommand.MSG_ACK,
                result, messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToOtherUserTerminal(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private List<ClientInfo> dispatchMessage(MessageContent dispatchedMessage) {
        List<ClientInfo> clientInfos = messageProducer.sendToAllUserTerminal(dispatchedMessage.getToId(), MessageCommand.MSG_P2P, dispatchedMessage, dispatchedMessage.getAppId());
        return clientInfos;
    }
}
