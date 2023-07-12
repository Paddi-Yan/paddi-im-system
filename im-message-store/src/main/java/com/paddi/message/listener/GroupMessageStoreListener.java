package com.paddi.message.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.model.message.DoStoreGroupMessageDTO;
import com.paddi.message.service.MessageStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:29:24
 */

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = Constants.RocketMQConstants.STORE_GROUP_MESSAGE_GROUP,
        topic = Constants.RocketMQConstants.STORE_GROUP_MESSAGE
)
public class GroupMessageStoreListener implements RocketMQListener<String> {

    @Autowired
    private MessageStoreService messageStoreService;

    @Override
    public void onMessage(String message) {
        log.info("CHAT MSG FORM SERVICE MODULE ::: {}", message);
        JSONObject obj = JSON.parseObject(message);
        DoStoreGroupMessageDTO doStoreGroupMessageDTO = obj.toJavaObject(DoStoreGroupMessageDTO.class);
        messageStoreService.storeGroupMessage(doStoreGroupMessageDTO);
    }
}
