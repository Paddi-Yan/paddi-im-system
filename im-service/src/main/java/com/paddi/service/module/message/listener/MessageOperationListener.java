package com.paddi.service.module.message.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.MessageContent;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.MessageReceiveACKContent;
import com.paddi.service.module.message.service.GroupMessageService;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.module.message.service.P2PMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.paddi.common.constants.Constants.COMMAND;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 20:04:34
 */
@RocketMQMessageListener(
        consumerGroup = Constants.RocketMQConstants.MESSAGE_SERVICE_GROUP,
        topic = Constants.RocketMQConstants.Im2MessageService
)
@Slf4j
@Component
public class MessageOperationListener implements RocketMQListener<String> {

    @Autowired
    private P2PMessageService p2pMessageService;

    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private MessageSyncService messageSyncService;


    @Override
    public void onMessage(String message) {
        log.info("CHAT MSG FORM QUEUE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        Integer command = obj.getInteger(COMMAND);
        if(command.equals(MessageCommand.MSG_P2P.getCommand())) {
            MessageContent messageContent = obj.toJavaObject(MessageContent.class);
            p2pMessageService.process(messageContent);
        } else if(command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
            GroupChatMessageContent groupChatMessageContent = obj.toJavaObject(GroupChatMessageContent.class);
            groupMessageService.process(groupChatMessageContent);
        } else if(command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
            MessageReceiveACKContent messageReceiveACKContent = obj.toJavaObject(MessageReceiveACKContent.class);
            messageSyncService.messageReceiveNotify(messageReceiveACKContent);
        } else if(command.equals(MessageCommand.MSG_READ.getCommand())) {
            MessageReadContent messageReadContent = obj.toJavaObject(MessageReadContent.class);
            messageSyncService.messageReadNotify(messageReadContent);
        }
    }
}
