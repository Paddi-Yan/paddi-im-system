package com.paddi.listener;

import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.protocol.MessagePackage;
import com.paddi.common.constants.Constants;
import com.paddi.config.BootstrapConfig;
import com.paddi.factory.ProcessorFactory;
import com.paddi.processor.AbstractProcessor;
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

    private static void startListenMessage(Integer brokerId, String namesrvAddr) {
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setConsumerGroup("im-server-consumer");
            consumer.subscribe(Constants.RocketMQConstants.MessageService2Im + "-" + brokerId, "*");
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
                for(MessageExt messageExt : list) {
                    try {
                        String message = new String(messageExt.getBody());
                        log.info("GROUP CHAT MSG FORM FORM SERVICE MODULE ::: {}", message);
                        MessagePackage messagePackage = JSONObject.parseObject(message, MessagePackage.class);
                        AbstractProcessor processor = ProcessorFactory.getMessageProcessor(messagePackage.getCommand());
                        processor.process(messagePackage);
                    } catch(Exception e) {
                        log.warn("CONSUME MESSAGE FAILED. MESSAGE:{}, error:{}", messageExt, e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch(MQClientException e) {
            log.error(e.getMessage());
        }
    }

    public static void init(BootstrapConfig.TcpConfig config) {
        startListenMessage(config.getBrokerId(), config.getRocketmq().getNamesrvAddr());
    }

}
