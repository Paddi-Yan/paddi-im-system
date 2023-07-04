package com.paddi.strategy.multi;

import com.paddi.codec.protocol.MessagePack;
import com.paddi.common.enums.command.SystemCommand;
import com.paddi.common.model.UserClientDTO;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.List;

import static com.paddi.common.constants.Constants.USERID;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 09:51:20
 */
public interface MultiTerminalLoginProcessStrategy {
    /**
     * 多端同步模式：1 单端登录：只允许一端在线，手机/电脑/web 踢掉除了本client+imei以外的设备
     * 2 双端登录：允许手机/电脑的一台设备 + web在线 踢掉除了本client+imei的非web端设备
     * 3 三段登录：允许手机和电脑单设备 + web同时在线 踢掉非本client+imei的同端设备
     * 4 多端登录：允许所有端多设备登录 不踢任何设备
     *
     * @param channels
     * @param userClientDTO
     */
    void process(List<NioSocketChannel> channels, UserClientDTO userClientDTO);
    
    default String buildTerminalIdentification(Integer clientType, String imei) {
        return clientType + ":" + imei;
    }

    default void sendOfflineMessage(NioSocketChannel channel) {
        String userId = (String) channel.attr(AttributeKey.valueOf(USERID)).get();
        MessagePack messagePack = new MessagePack();
        messagePack.setUserId(userId);
        messagePack.setToId(userId);
        messagePack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
        channel.writeAndFlush(messagePack);
    }

}
