package com.paddi.codec;

import com.paddi.codec.protocol.Message;
import com.paddi.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.paddi.codec.constans.MessageConstants.MESSAGE_HEADER_LENGTH;

/**
 * clientType: IOS 安卓 pc(windows mac) web
 * 解析类型: json/protobuf
 * 协议: 请求头(指令 版本 clientType 消息解析类型 imei长度 appId body长度) + imei + 请求体
 * 28 + imei + body
 *
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:06:36
 */
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
                          List<Object> next) throws Exception {

        if(byteBuf.readableBytes() < MESSAGE_HEADER_LENGTH) {
            return;
        }

        Message message = ByteBufToMessageUtils.decode(byteBuf);
        if(message == null) {
            return;
        }
        next.add(message);
    }
}
