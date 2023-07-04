package com.paddi.strategy.multi;

import com.paddi.common.enums.ClientType;
import com.paddi.common.model.UserClientDTO;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.List;

import static com.paddi.common.constants.Constants.CLIENT_TYPE;
import static com.paddi.common.constants.Constants.IMEI;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 09:54:31
 */
public class TwoTerminalLoginProcessStrategy implements MultiTerminalLoginProcessStrategy{

    /**
     * 双端登录：允许手机/电脑的一台设备 + web在线 踢掉除了本client+imei的非web端设备
     * @param channels
     * @param userClientDTO
     */
    @Override
    public void process(List<NioSocketChannel> channels, UserClientDTO userClientDTO) {
        for(NioSocketChannel channel : channels) {
            //忽略Web端设备登录
            if(userClientDTO.getClientType().equals(ClientType.WEB.getCode())) {
                return;
            }
            Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(CLIENT_TYPE)).get();
            String imei = (String) channel.attr(AttributeKey.valueOf(IMEI)).get();
            //无需通知已登录的Web下线
            if(clientType.equals(ClientType.WEB.getCode())) {
                continue;
            }
            //除了Web端 其他不等于当前(clientType:imei)的设备都需要下线
            if(!buildTerminalIdentification(userClientDTO.getClientType(), userClientDTO.getImei()).equals(buildTerminalIdentification(clientType, imei))) {
                sendOfflineMessage(channel);
            }
        }
    }
}
