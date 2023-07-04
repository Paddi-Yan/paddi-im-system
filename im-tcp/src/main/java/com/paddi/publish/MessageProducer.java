package com.paddi.publish;

import com.paddi.factory.RocketMQFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 21:23:25
 */
@Slf4j
public class MessageProducer {

    public static void sendMessage(String topic, String tag, String payload) {
        DefaultMQProducer producer = RocketMQFactory.getProducer();
        Message message = new Message(topic, tag, payload.getBytes());
        try {
            producer.send(message);
        } catch(MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("send message error: {}", e.getMessage());
        }
    }

}
