package com.paddi.service.module.message.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.message.MessageReadPackage;
import com.paddi.codec.pack.message.RecallMessageNotifyPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.constants.HttpStatus;
import com.paddi.common.enums.ConversationTypeEnum;
import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.enums.MessageErrorCode;
import com.paddi.common.enums.command.Command;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.*;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.conversation.service.ConversationService;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.message.entity.MessageBody;
import com.paddi.service.module.message.mapper.MessageBodyMapper;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.utils.GroupMessageProducer;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
import com.paddi.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private GroupMessageProducer groupMessageProducer;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private MessageBodyMapper messageBodyMapper;

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private MessageStoreService messageStoreService;

    @Autowired
    private GroupMemberService groupMemberService;

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

    /**
     * 撤回消息的流程:
     *   1.修改历史消息的状态
     *   2.修改离线消息的状态
     *   3.发送ACK给用户
     *   4.发送给在线同步端
     *   5.分发给消息的接收方
     * @param content
     */
    @Override
    public void recallMessage(RecallMessageContent content) {
        Long messageTime = content.getMessageTime();
        long now = System.currentTimeMillis();

        RecallMessageNotifyPackage notifyPackage = new RecallMessageNotifyPackage();
        BeanUtils.copyProperties(content, notifyPackage);
        ClientInfo clientInfo = new ClientInfo(content.getAppId(), content.getClientType(), content.getImei());

        //超过最大间隔时间的消息无法撤回
        if(now - messageTime >= configuration.getMessageRecallTimeout()) {
            //ACK失败
            sendRecallMessageACK(notifyPackage, Result.error(MessageErrorCode.MESSAGE_RECALL_TIME_OUT), clientInfo);
            return;
        }
        LambdaQueryWrapper<MessageBody> wrapper = Wrappers.lambdaQuery(MessageBody.class)
                                                     .eq(MessageBody :: getAppId, content.getAppId())
                                                     .eq(MessageBody :: getMessageKey, content.getMessageKey());
        MessageBody messageBody = messageBodyMapper.selectOne(wrapper);
        //不存在的消息无法撤回
        if(messageBody == null) {
            //ACK失败
            sendRecallMessageACK(notifyPackage, Result.error(MessageErrorCode.MESSAGEBODY_IS_NOT_EXIST), clientInfo);
            return;
        }
        //已经撤回的消息
        if(messageBody.getDelFlag() == DelFlagEnum.DELETE.getCode()) {
            sendRecallMessageACK(notifyPackage, Result.error(MessageErrorCode.MESSAGE_IS_RECALLED), clientInfo);
            return;
        }
        //修改历史消息的状态
        messageBody.setDelFlag(DelFlagEnum.DELETE.getCode());
        messageBodyMapper.update(messageBody, wrapper);

        //修改离线消息的状态
        if(content.getConversationType() == ConversationTypeEnum.P2P.getCode()) {
            //双方的离线消息都会进行修改
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(content, offlineMessageContent);
            offlineMessageContent.setMessageKey(snowflakeIdWorker.nextId());
            offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            Long sequence = sequenceGenerator.generate(content.getAppId() + Constants.SequenceConstants.Message);
            offlineMessageContent.setMessageSequence(sequence);
            messageStoreService.storeOfflineMessage(offlineMessageContent);

            //发送ACK给发送方
            sendRecallMessageACK(notifyPackage, Result.success(), clientInfo);

            //发送给在线同步端
            syncToOperator(content.getFromId(), notifyPackage, content, MessageCommand.MSG_RECALL_NOTIFY);

            //分发给消息的接收方
            sendToReceiver(content.getToId(), notifyPackage, content, MessageCommand.MSG_RECALL_NOTIFY);
        } else if(content.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            //群成员的离线消息都会进行修改
            GroupOfflineMessageContent groupOfflineMessageContent = new GroupOfflineMessageContent();
            BeanUtils.copyProperties(content, groupOfflineMessageContent);
            groupOfflineMessageContent.setMessageKey(snowflakeIdWorker.nextId());
            groupOfflineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            Long sequence = sequenceGenerator.generate(content.getAppId() + Constants.SequenceConstants.GroupMessage);
            groupOfflineMessageContent.setMessageSequence(sequence);
            groupOfflineMessageContent.setGroupId(content.getToId());
            List<String> groupMemberList = groupMemberService.getGroupMemberId(content.getToId(), content.getAppId());
            messageStoreService.storeGroupOfflineMessage(groupOfflineMessageContent, groupMemberList);

            //发送ACK给发送方
            sendRecallMessageACK(notifyPackage, Result.success(), clientInfo);

            //发送给在线同步端
            //syncToOperator(content.getFromId(), notifyPackage, clientInfo, MessageCommand.MSG_RECALL_NOTIFY);
            //同事分发撤回通知给当前用户和其他群成员
            groupMessageProducer.sendMessageByMemberType(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY,
                    notifyPackage, clientInfo, groupMemberList);
        } else {
            throw new ApplicationException(HttpStatus.ERROR, "非法的会话类型");
        }
    }

    private void sendToReceiver(String receiver, Object message, ClientInfo clientInfo, Command command) {
        messageProducer.sendToAllUserTerminal(receiver, command, message, clientInfo.getAppId());
    }

    private void sendRecallMessageACK(RecallMessageNotifyPackage notifyPackage, Result result, ClientInfo clientInfo) {
        result.setData(notifyPackage);
        messageProducer.sendToSpecifiedUserTerminal(notifyPackage.getFromId(), MessageCommand.MSG_RECALL_ACK, result, clientInfo);
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

    public void syncToOperator(String operator, Object message, ClientInfo clientInfo, Command command) {
        messageProducer.sendToOtherUserTerminal(operator, command, message, clientInfo);
    }
}
