package com.paddi.codec;

import com.paddi.codec.protocol.Message;
import com.paddi.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

import static com.paddi.codec.constans.MessageConstants.MESSAGE_HEADER_LENGTH;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception {

        ByteBuf content = msg.content();
        if (content.readableBytes() < MESSAGE_HEADER_LENGTH) {
            return;
        }
        Message message = ByteBufToMessageUtils.decode(content);
        if(message == null){
            return;
        }
        out.add(message);
    }
}
