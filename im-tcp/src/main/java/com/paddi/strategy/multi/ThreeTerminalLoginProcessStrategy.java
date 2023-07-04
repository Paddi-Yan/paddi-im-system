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
public class ThreeTerminalLoginProcessStrategy implements MultiTerminalLoginProcessStrategy{

    /**
     * 三端登录：允许手机和电脑单设备 + web同时在线 踢掉非本client+imei的同端设备
     * @param channels
     * @param userClientDTO
     */
    @Override
    public void process(List<NioSocketChannel> channels, UserClientDTO userClientDTO) {
        for(NioSocketChannel channel : channels) {
            //当前设备是Web端无需进行其他设备的下线
            if(userClientDTO.getClientType().equals(ClientType.WEB.getCode())) {
                return;
            }
            Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(CLIENT_TYPE)).get();
            String imei = (String) channel.attr(AttributeKey.valueOf(IMEI)).get();
            //已登录的Web设备不需要下线
            if(clientType.equals(ClientType.WEB.getCode())) {
                continue;
            }
            Boolean isSameClient = false;
            //noinspection AlibabaAvoidComplexCondition
            if((clientType.equals(ClientType.ANDROID.getCode()) || clientType.equals(ClientType.IOS.getCode()))
                    && (userClientDTO.getClientType().equals(ClientType.ANDROID.getCode()) || userClientDTO.getClientType().equals(ClientType.IOS.getCode()))) {
                isSameClient = true;
            }
            //noinspection AlibabaAvoidComplexCondition
            if((clientType.equals(ClientType.WINDOWS.getCode()) || clientType.equals(ClientType.MAC.getCode()))
                    && (userClientDTO.getClientType().equals(ClientType.WINDOWS.getCode()) || userClientDTO.getClientType().equals(ClientType.MAC.getCode()))) {
                isSameClient = true;
            }
            //如果当前Channel对应的终端和当前登录终端类型相同并且终端类型和Imei互斥
            if(isSameClient && !buildTerminalIdentification(userClientDTO.getClientType(), userClientDTO.getImei()).equals(buildTerminalIdentification(clientType, imei))) {
                sendOfflineMessage(channel);
            }
        }
    }
}
