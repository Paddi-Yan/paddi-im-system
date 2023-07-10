package com.paddi.service.module.message.service.impl;

import com.paddi.common.constants.Constants;
import com.paddi.common.enums.ConversationTypeEnum;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.GroupOfflineMessageContent;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.message.service.CheckSendMessageService;
import com.paddi.service.module.message.service.GroupMessageService;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
import org.springframework.beans.BeanUtils;
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
 * @CreatedTime: 2023年07月08日 23:18:05
 */
@Service
public class GroupMessageServiceImpl implements GroupMessageService {

    @Autowired
    private CheckSendMessageService checkSendMessageService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private MessageStoreService messageStoreService;

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;

    private final ThreadPoolExecutor threadPoolExecutor;{
        AtomicInteger counter = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1024), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("group-message-process-thread-" + counter.decrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void process(GroupChatMessageContent groupChatMessageContent) {
        String fromId = groupChatMessageContent.getFromId();
        String groupId = groupChatMessageContent.getGroupId();
        Integer appId = groupChatMessageContent.getAppId();

        List<String> groupMemberIdList = groupMemberService.getGroupMemberId(groupChatMessageContent.getGroupId(), groupChatMessageContent.getAppId());
        GroupChatMessageContent messageFromCache = messageStoreService.getMessageFromCache(groupChatMessageContent.getAppId(),
                groupChatMessageContent.getMessageId(),
                GroupChatMessageContent.class);
        if(messageFromCache != null) {
            threadPoolExecutor.execute(() -> {
                sendACK(messageFromCache, Result.success());
                syncToSender(messageFromCache);
                dispatchMessage(messageFromCache, groupMemberIdList);
            });
            return;
        }

        //1.线程池
        //2.TCP网关提前校验
        //3.消息持久化异步化
        String key = groupChatMessageContent.getAppId() + Constants.SequenceConstants.GroupMessage + groupChatMessageContent.getGroupId();
        Long sequence = sequenceGenerator.generate(key);
        groupChatMessageContent.setMessageSequence(sequence);

        threadPoolExecutor.execute(() -> {
            messageStoreService.storeGroupMessage(groupChatMessageContent);

            //存储离线消息
            GroupOfflineMessageContent groupOfflineMessageContent = new GroupOfflineMessageContent();
            BeanUtils.copyProperties(groupOfflineMessageContent, groupOfflineMessageContent);
            groupOfflineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
            messageStoreService.storeGroupOfflineMessage(groupOfflineMessageContent, groupMemberIdList);

            sendACK(groupChatMessageContent, Result.success());
            syncToSender(groupChatMessageContent);
            dispatchMessage(groupChatMessageContent, groupMemberIdList);

            messageStoreService.setMessageToCache(groupChatMessageContent);
        });
    }

    private void dispatchMessage(GroupChatMessageContent groupChatMessageContent, List<String> groupMemberIdList) {
        groupMemberIdList.forEach(memberId -> {
            if(!memberId.equals(groupChatMessageContent.getFromId())) {
                messageProducer.sendToAllUserTerminal(memberId,
                        GroupEventCommand.MSG_GROUP,
                        groupChatMessageContent,
                        groupChatMessageContent.getAppId());
            }
        });

    }

    private void syncToSender(GroupChatMessageContent groupChatMessageContent) {
        messageProducer.sendToOtherUserTerminal(groupChatMessageContent.getFromId(),
                GroupEventCommand.MSG_GROUP,
                groupChatMessageContent,
                new ClientInfo(groupChatMessageContent.getAppId(),
                        groupChatMessageContent.getClientType(),
                        groupChatMessageContent.getImei()));
    }

    private void sendACK(GroupChatMessageContent groupChatMessageContent, Result result) {
        messageProducer.sendToSpecifiedUserTerminal(groupChatMessageContent.getFromId(),
                GroupEventCommand.GROUP_MSG_ACK,
                result,
                new ClientInfo(groupChatMessageContent.getAppId(),
                        groupChatMessageContent.getClientType(),
                        groupChatMessageContent.getImei()));
    }

    @Override
    public Result processBefore(String fromId, String groupId, Integer appId) {
        return checkSendMessageService.checkGroupMessage(fromId, groupId, appId);
    }
}
