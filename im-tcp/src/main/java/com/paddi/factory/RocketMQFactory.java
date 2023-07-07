package com.paddi.factory;

import com.paddi.config.BootstrapConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 21:47:34
 */
@Slf4j
public class RocketMQFactory {

    private static Map<String, DefaultMQProducer> producerMap = new ConcurrentHashMap<>();

    private static BootstrapConfig.RocketMQConfig config;

    public static void init(BootstrapConfig.RocketMQConfig rocketMQConfig) {
        config = rocketMQConfig;
    }

    public static DefaultMQProducer getProducer(String topic) {
        return producerMap.computeIfAbsent(topic, v -> createDefaultMQProducer());
    }

    private static DefaultMQProducer createDefaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(config.getNamesrvAddr());
        producer.setProducerGroup(config.getProducerGroup());
        try {
            producer.start();
        } catch(MQClientException e) {
            log.error(e.getErrorMessage());
        }
        return producer;
    }

}
