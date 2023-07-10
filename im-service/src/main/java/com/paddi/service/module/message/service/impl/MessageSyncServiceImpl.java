package com.paddi.service.module.message.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.pack.message.MessageReadPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.Command;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.*;
import com.paddi.service.module.conversation.service.ConversationService;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 17:48:12
 */
@Service
public class MessageSyncServiceImpl implements MessageSyncService {

    @Autowired
    private MessageProducer messageProducer;


    @Autowired
    private ConversationService conversationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
        //更新会话的readSequence
        conversationService.messageMarkRead(messageReadContent);
        MessageReadPackage messageReadPackage = new MessageReadPackage();
        BeanUtils.copyProperties(messageReadContent, messageReadPackage);
        //通知在线的同步端
        syncToOperator(messageReadPackage, messageReadContent, MessageCommand.MSG_READ_NOTIFY);
        //发送已读回执
        messageProducer.sendToAllUserTerminal(messageReadContent.getToId(), MessageCommand.MSG_READ_RECEIPT,
                messageReadPackage, messageReadContent.getAppId());
    }

    @Override
    public void groupMessageReadNotify(MessageReadContent messageReadContent) {
        //更新群聊会话的readSequence
        conversationService.messageMarkRead(messageReadContent);
        MessageReadPackage messageReadPackage = new MessageReadPackage();
        BeanUtils.copyProperties(messageReadContent, messageReadPackage);
        syncToOperator(messageReadPackage, messageReadContent, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        if(!messageReadContent.getFromId().equals(messageReadPackage.getToId())) {
            messageProducer.sendToAllUserTerminal(messageReadContent.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT,
                    messageReadPackage, messageReadContent.getAppId());
        }
    }

    @Override
    public Result syncOfflineMessage(SyncRequest request) {
        if(request.getLimit() > 100) {
            request.setLimit(100);
        }
        SyncResponse<OfflineMessageContent> syncResponse = new SyncResponse<>();

        String cacheKey = request.getAppId() + Constants.RedisConstants.OFFLINE_MESSAGE + request.getOperator();
        ZSetOperations<String, String> operations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = operations.reverseRangeWithScores(cacheKey, 0, 0);
        //获取最大的messageSequence
        Long messageSequence = 0L;
        if(CollectionUtil.isNotEmpty(typedTuples)) {
            ArrayList<ZSetOperations.TypedTuple<String>> list = new ArrayList<>(typedTuples);
            ZSetOperations.TypedTuple<String> typedTuple = list.get(0);
            messageSequence = typedTuple.getScore().longValue();
        }

        ArrayList<OfflineMessageContent> offlineMessageContentList = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<String>> typedTupleSet = operations.rangeByScoreWithScores(cacheKey, request.getLastSequence(), messageSequence, 0, request.getLimit());
        //获取离线消息
        for(ZSetOperations.TypedTuple<String> typedTuple : typedTupleSet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            offlineMessageContentList.add(offlineMessageContent);
        }

        //判断是否拉取完毕
        if(CollectionUtil.isNotEmpty(offlineMessageContentList)) {
            OfflineMessageContent maxSequenceMessage = offlineMessageContentList.get(offlineMessageContentList.size() - 1);
            syncResponse.setIsCompleted(maxSequenceMessage.getMessageKey() >= messageSequence);
        }else {
            syncResponse.setIsCompleted(true);
        }

        syncResponse.setDataList(offlineMessageContentList);
        syncResponse.setMaxSequence(messageSequence);
        return Result.success(syncResponse);
    }

    private void syncToOperator(MessageReadPackage messageReadPackage, MessageReadContent messageReadContent,
                                Command command) {
        messageProducer.sendToOtherUserTerminal(messageReadContent.getFromId(),
                command,
                messageReadPackage,
                new ClientInfo(messageReadContent.getAppId(),
                        messageReadContent.getClientType(),
                        messageReadContent.getImei()));
    }
}
