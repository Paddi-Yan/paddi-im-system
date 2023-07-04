package com.paddi.strategy.multi;

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
public class OneTerminalLoginProcessStrategy implements MultiTerminalLoginProcessStrategy{

    /**
     * 单端登录：只允许一端在线，手机/电脑/web 踢掉除了本client+imei以外的设备
     *
     * @param channels
     * @param userClientDTO
     */
    @Override
    public void process(List<NioSocketChannel> channels, UserClientDTO userClientDTO) {
        for(NioSocketChannel channel : channels) {
            Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(CLIENT_TYPE)).get();
            String imei = (String) channel.attr(AttributeKey.valueOf(IMEI)).get();
            //将除了本次登录clientType:imei以外的设备全部下线
            if(!buildTerminalIdentification(userClientDTO.getClientType(), userClientDTO.getImei()).equals(buildTerminalIdentification(clientType, imei))) {
                sendOfflineMessage(channel);
            }

        }
    }
}
