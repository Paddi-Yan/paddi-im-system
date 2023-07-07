package com.paddi.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.factory.RocketMQFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import static com.paddi.common.constants.Constants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 21:23:25
 */
@Slf4j
public class MessageProducer {

    public static void sendMessage(com.paddi.codec.protocol.Message message, Integer command) {
        String topic = Constants.RocketMQConstants.Im2MessageService;
        DefaultMQProducer producer = RocketMQFactory.getProducer(topic);
        try {
            JSONObject obj = (JSONObject) JSON.toJSON(message.getMessagePack());
            obj.put(COMMAND, command);
            obj.put(CLIENT_TYPE, message.getMessageHeader().getClientType());
            obj.put(IMEI, message.getMessageHeader().getImei());
            obj.put(APPID, message.getMessageHeader().getAppId());

            Message msg = new Message(topic, obj.toJSONString().getBytes());
            log.info("SEND CHAT MESSAGE ::: [{}]", msg);
            producer.send(msg);
        } catch(MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("send message error: {}", e.getMessage());
        }
    }

}
