package com.paddi.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.paddi.codec.pack.LoginPackage;
import com.paddi.codec.protocol.Message;
import com.paddi.codec.protocol.MessageHeader;
import com.paddi.common.enums.ConnectionStatusEnum;
import com.paddi.common.enums.command.SystemCommand;
import com.paddi.common.model.UserClientDTO;
import com.paddi.common.model.UserSession;
import com.paddi.publish.MessageProducer;
import com.paddi.redis.RedisManager;
import com.paddi.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

import static com.paddi.common.constants.Constants.*;
import static com.paddi.common.constants.Constants.RedisConstants.USER_SESSION;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:07:27
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    public NettyServerHandler(Integer brokerId) {
        this.brokerId = brokerId;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        Integer command = message.getMessageHeader().getCommand();
        if(command == SystemCommand.LOGIN.getCommand()) {
            //登录
            LoginPackage loginPackage = JSON.parseObject(JSON.toJSONString(message.getMessagePack()),
                    new TypeReference<LoginPackage>() {
                    }.getType());

            ctx.channel().attr(AttributeKey.valueOf(USERID)).set(loginPackage.getUserId());
            MessageHeader messageHeader = message.getMessageHeader();
            ctx.channel().attr(AttributeKey.valueOf(APPID)).set(messageHeader.getAppId());
            //终端类型
            ctx.channel().attr(AttributeKey.valueOf(CLIENT_TYPE)).set(messageHeader.getClientType());
            //终端标识
            ctx.channel().attr(AttributeKey.valueOf(IMEI)).set(messageHeader.getImei());

            //存储Channel
            SessionSocketHolder.put(messageHeader.getAppId(),
                    loginPackage.getUserId(),
                    messageHeader.getClientType(),
                    messageHeader.getImei(),
                    (NioSocketChannel) ctx.channel());

            UserSession userSession = new UserSession();
            userSession.setAppId(messageHeader.getAppId());
            userSession.setClientType(messageHeader.getClientType());
            userSession.setImei(messageHeader.getImei());
            userSession.setUserId(loginPackage.getUserId());
            userSession.setConnectionState(ConnectionStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setBrokerHost(InetAddress.getLocalHost().getHostAddress());

            //Session存储到Redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            String sessionCacheKey = messageHeader.getAppId() + USER_SESSION + loginPackage.getUserId();
            RMap<String , String> map = redissonClient.getMap(sessionCacheKey);
            //key-field-value
            //key: appId:user-session:userId
            //field: clientType:imei
            //value: session
            map.put(messageHeader.getClientType() + ":" + messageHeader.getImei(), JSONObject.toJSONString(userSession));

            //向其他Netty服务器广播该用户上线消息
            UserClientDTO userClientDTO = new UserClientDTO(
                    messageHeader.getAppId(),
                    messageHeader.getClientType(),
                    loginPackage.getUserId(),
                    messageHeader.getImei());
            RTopic topic = redissonClient.getTopic(RedisConstants.USER_LOGIN_CHANNEL);
            topic.publish(JSONObject.toJSONString(userClientDTO));

        } else if(command == SystemCommand.LOGOUT.getCommand()) {
            //退出登录
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if(command == SystemCommand.PING.getCommand()) {
            //设置最后一次心跳包的接收时间
            ctx.channel().attr(AttributeKey.valueOf(READ_TIME)).set(System.currentTimeMillis());
        }else {
            MessageProducer.sendMessage(message, command);
        }
    }
}
