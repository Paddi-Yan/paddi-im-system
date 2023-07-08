package com.paddi.service.module.message.service.impl;

import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.message.service.CheckSendMessageService;
import com.paddi.service.module.message.service.GroupMessageService;
import com.paddi.service.module.message.service.MessageStoreService;
import com.paddi.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public void process(GroupChatMessageContent groupChatMessageContent) {
        String fromId = groupChatMessageContent.getFromId();
        String groupId = groupChatMessageContent.getGroupId();
        Integer appId = groupChatMessageContent.getAppId();
        Result result = processBefore(fromId, groupId, appId);
        if(result.isSuccess()) {
            messageStoreService.storeGroupMessage(groupChatMessageContent);
            sendACK(groupChatMessageContent, result);
            syncToSender(groupChatMessageContent);
            dispatchMessage(groupChatMessageContent);
        }else {
            sendACK(groupChatMessageContent, result);
        }
    }

    private void dispatchMessage(GroupChatMessageContent groupChatMessageContent) {
        List<String> groupMemberIdList = groupMemberService.getGroupMemberId(groupChatMessageContent.getGroupId(), groupChatMessageContent.getAppId());
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

    private Result processBefore(String fromId, String groupId, Integer appId) {
        return checkSendMessageService.checkGroupMessage(fromId, groupId, appId);
    }
}
