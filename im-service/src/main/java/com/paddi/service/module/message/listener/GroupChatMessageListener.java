package com.paddi.service.module.message.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.model.message.GroupChatMessageContent;
import com.paddi.service.module.message.service.GroupMessageService;
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
public class GroupChatMessageListener implements RocketMQListener<String> {

    @Autowired
    private GroupMessageService groupMessageService;

    @Override
    public void onMessage(String message) {
        log.info("CHAT MSG FORM QUEUE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        Integer command = obj.getInteger(COMMAND);
        if(command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
            GroupChatMessageContent groupChatMessageContent = obj.toJavaObject(GroupChatMessageContent.class);
            groupMessageService.process(groupChatMessageContent);
        }
    }
}
