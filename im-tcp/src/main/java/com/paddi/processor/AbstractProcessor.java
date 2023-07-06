package com.paddi.processor;

import com.paddi.codec.protocol.MessagePackage;
import com.paddi.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 00:08:15
 */
public abstract class AbstractProcessor {

    public abstract void processBefore();

    public void process(MessagePackage messagePackage) {
        processBefore();
        NioSocketChannel channel = SessionSocketHolder.get(messagePackage.getAppId(),
                messagePackage.getToId(), messagePackage.getClientType(), messagePackage.getImei());
        if(channel != null) {
            channel.writeAndFlush(messagePackage);
        }
        processAfter();
    }

    public abstract void processAfter();

}
