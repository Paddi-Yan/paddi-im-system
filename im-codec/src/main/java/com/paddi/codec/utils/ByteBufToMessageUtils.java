package com.paddi.codec.utils;

import com.paddi.codec.factory.MessageParseStrategyFactory;
import com.paddi.codec.protocol.Message;
import com.paddi.codec.protocol.MessageHeader;
import com.paddi.codec.strategy.parse.MessageParseStrategy;
import io.netty.buffer.ByteBuf;

/**
 * @description: 将ByteBuf转化为Message实体，根据私有协议转换
 *               私有协议规则，
 *               4位表示Command表示消息的开始，
 *               4位表示version
 *               4位表示clientType
 *               4位表示messageType
 *               4位表示appId
 *               4位表示imei长度
 *               imei
 *               4位表示数据长度
 *               data
 *               后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:28:20
 */
public class ByteBufToMessageUtils {

    public static Message decode(ByteBuf byteBuf) {
        /** 获取command */
        int command = byteBuf.readInt();

        /** 获取version */
        int version = byteBuf.readInt();

        /** 获取clientType */
        int clientType = byteBuf.readInt();

        /** 获取messageType */
        int messageType = byteBuf.readInt();

        /** 获取appId */
        int appId = byteBuf.readInt();

        /** 获取imeiLength */
        int imeiLength = byteBuf.readInt();

        /** 获取bodyLength */
        int bodyLength = byteBuf.readInt();

        if(byteBuf.readableBytes() < bodyLength + imeiLength) {
            byteBuf.resetReaderIndex();
            return null;
        }

        byte[] imeiData = new byte[imeiLength];
        byteBuf.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLength];
        byteBuf.readBytes(bodyData);

        MessageHeader messageHeader = MessageHeader.builder()
                                           .command(command)
                                           .version(version)
                                           .clientType(clientType)
                                           .messageType(messageType)
                                           .appId(appId)
                                           .imeiLength(imeiLength)
                                           .bodyLength(bodyLength)
                                           .imei(imei).build();

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        MessageParseStrategy strategy = MessageParseStrategyFactory.getInstance(messageType);
        message.setMessagePack(strategy.parse(bodyData));

        byteBuf.markReaderIndex();
        return message;
    }
}
