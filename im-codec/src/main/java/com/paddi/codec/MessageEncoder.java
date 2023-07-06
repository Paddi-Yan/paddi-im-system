package com.paddi.codec;

import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.protocol.MessagePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码类，私有协议规则
 * command(4字节) + messageLength(4字节) + data
 *
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 12:13:07
 */
public class MessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        if(msg instanceof MessagePackage) {
            MessagePackage messagePackage = (MessagePackage) msg;
            String s = JSONObject.toJSONString(messagePackage);
            byte[] bytes = s.getBytes();
            byteBuf.writeInt(messagePackage.getCommand());
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
