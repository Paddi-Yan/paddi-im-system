package com.paddi.listener;

import com.paddi.common.constants.Constants;
import com.paddi.factory.RocketMQFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 22:08:10
 */
@Slf4j
public class MessageListener {

    private static void startListenMessage(Integer brokerId) {
        try {
            DefaultMQPushConsumer consumer = RocketMQFactory.getConsumer();
            consumer.setConsumerGroup("im-server-consumer");
            consumer.subscribe(Constants.RocketMQConstants.MessageService2Im + "-" + brokerId, "*");
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
                for(MessageExt messageExt : list) {
                    log.info("receive message:{}", messageExt);
                    log.info("detail message content: {}", new String(messageExt.getBody()));

                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch(MQClientException e) {
            log.error(e.getMessage());
        }
    }

    public static void init(Integer brokerId) {
        startListenMessage(brokerId);
    }

}
