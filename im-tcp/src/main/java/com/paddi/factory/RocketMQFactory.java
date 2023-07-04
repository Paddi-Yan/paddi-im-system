package com.paddi.factory;

import com.paddi.config.BootstrapConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 21:47:34
 */
@Slf4j
public class RocketMQFactory {

    private static DefaultMQProducer producer;

    private static BootstrapConfig.RocketMQConfig config;

    public static void init(BootstrapConfig.RocketMQConfig rocketMQConfig) {
        config = rocketMQConfig;
        producer = createDefaultMQProducer(rocketMQConfig);
        try {
            producer.start();
        } catch(MQClientException e) {
            log.error("Producer init error: {}", e.getErrorMessage());
        }
    }

    public static DefaultMQProducer getProducer() {
        return producer;
    }

    public static DefaultMQPushConsumer getConsumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr(config.getNamesrvAddr());
        return consumer;
    }


    private static DefaultMQProducer createDefaultMQProducer(BootstrapConfig.RocketMQConfig rocketMQConfig) {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(rocketMQConfig.getNamesrvAddr());
        producer.setProducerGroup(rocketMQConfig.getProducerGroup());
        return producer;
    }

}
