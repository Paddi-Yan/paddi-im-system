package com.paddi.service.module.user.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.UserEventCommand;
import com.paddi.service.module.user.model.UserStatusChangeNotifyContent;
import com.paddi.service.module.user.service.UserStatusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.paddi.common.constants.Constants.COMMAND;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 19:49:58
 */
@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = Constants.RocketMQConstants.USER_ONLINE_STATUS_GROUP,
        topic = Constants.RocketMQConstants.Im2UserService
)
public class UserOnlineStatusChangeListener implements RocketMQListener<String> {

    @Autowired
    private UserStatusService userStatusService;

    @Override
    public void onMessage(String message) {
        log.info("USER ONLINE STATUS CHANGE MSG FORM TCP MODULE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        Integer command = obj.getInteger(COMMAND);
        if(Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())) {
            UserStatusChangeNotifyContent userStatusChangeNotifyContent = JSON.parseObject(message, UserStatusChangeNotifyContent.class);
            userStatusService.processUserOnlineStatusNotify(userStatusChangeNotifyContent);
        }

    }
}
