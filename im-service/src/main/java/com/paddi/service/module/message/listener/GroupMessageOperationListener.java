package com.paddi.service.module.message.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.service.module.message.service.GroupMessageService;
import com.paddi.service.module.message.service.MessageSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.paddi.common.constants.Constants.COMMAND;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月08日 23:01:19
 */
@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = Constants.RocketMQConstants.GROUP_MESSAGE_SERVICE_GROUP,
        topic = Constants.RocketMQConstants.Im2GroupService
)
public class GroupMessageOperationListener implements RocketMQListener<String> {

    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private MessageSyncService messageSyncService;

    @Override
    public void onMessage(String message) {
        log.info("GROUP CHAT MSG FORM FORM TCP MODULE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        Integer command = obj.getInteger(COMMAND);
        if(command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
            GroupChatMessageContent groupChatMessageContent = obj.toJavaObject(GroupChatMessageContent.class);
            groupMessageService.process(groupChatMessageContent);
        } else if(command.equals(GroupEventCommand.MSG_GROUP_READED.getCommand())) {
            MessageReadContent messageReadContent = obj.toJavaObject(MessageReadContent.class);
            messageSyncService.groupMessageReadNotify(messageReadContent);
        }
    }
}
