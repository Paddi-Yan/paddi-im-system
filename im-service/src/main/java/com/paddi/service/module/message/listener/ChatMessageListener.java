package com.paddi.service.module.message.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.service.module.message.model.MessageContent;
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
public class ChatMessageListener implements RocketMQListener<String> {

    @Autowired
    private P2PMessageService p2pMessageService;


    @Override
    public void onMessage(String message) {
        log.info("CHAT MSG FORM QUEUE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        Integer command = obj.getInteger(COMMAND);
        if(command.equals(MessageCommand.MSG_P2P.getCommand())) {
            MessageContent messageContent = obj.toJavaObject(MessageContent.class);
            p2pMessageService.process(messageContent);
        }
    }
}
