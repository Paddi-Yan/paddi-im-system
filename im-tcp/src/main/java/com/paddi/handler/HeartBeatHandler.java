package com.paddi.handler;

import com.paddi.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import static com.paddi.common.constants.Constants.READ_TIME;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 14:19:21
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Long hearBeatTime;

    public HeartBeatHandler(Long hearBeatTime) {
        this.hearBeatTime = hearBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.ALL_IDLE) {
                Long lastTime = (Long) ctx.channel().attr(AttributeKey.valueOf(READ_TIME)).get();
                Long currentTime = System.currentTimeMillis();
                if(lastTime != null && currentTime - lastTime > hearBeatTime) {
                    //离线状态更新
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }
            }

        }
    }
}
