package com.paddi.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.paddi.codec.pack.LoginPackage;
import com.paddi.codec.pack.message.ChatMessageACK;
import com.paddi.codec.pack.user.LoginACKPackage;
import com.paddi.codec.pack.user.UserStatusChangeNotifyPackage;
import com.paddi.codec.protocol.Message;
import com.paddi.codec.protocol.MessageHeader;
import com.paddi.codec.protocol.MessagePackage;
import com.paddi.common.enums.ConnectionStatusEnum;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.enums.command.SystemCommand;
import com.paddi.common.enums.command.UserEventCommand;
import com.paddi.common.model.Result;
import com.paddi.common.model.UserClientDTO;
import com.paddi.common.model.UserSession;
import com.paddi.common.model.message.CheckMessageRequest;
import com.paddi.feign.FeignMessageService;
import com.paddi.publish.MessageProducer;
import com.paddi.redis.RedisManager;
import com.paddi.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    private String logicUrl;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        this.logicUrl = logicUrl;
        this.feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 1000))
                .target(FeignMessageService.class, logicUrl);
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



            RedissonClient redissonClient = RedisManager.getRedissonClient();

            //Session存储到Redis
            cacheUserSession(loginPackage, messageHeader, redissonClient);

            //向其他Netty服务器广播该用户上线消息
            broadcastUserLoginMessage(loginPackage, messageHeader, redissonClient);

            //用户上线通知
            sendUserOnlineStatusMessageToLogicModule(loginPackage, messageHeader);

            //通知用户登陆成功
            sendUserLoginSuccessACK(ctx, loginPackage, messageHeader);

        } else if(command == SystemCommand.LOGOUT.getCommand()) {
            //退出登录
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());

        } else if(command == SystemCommand.PING.getCommand()) {
            //设置最后一次心跳包的接收时间
            ctx.channel().attr(AttributeKey.valueOf(READ_TIME)).set(System.currentTimeMillis());
        } else if(command == MessageCommand.MSG_P2P.getCommand() || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            CheckMessageRequest request = new CheckMessageRequest();
            request.setAppId(message.getMessageHeader().getAppId());
            request.setCommand(message.getMessageHeader().getCommand());
            JSONObject obj = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()));
            String fromId = obj.getString("fromId");
            request.setFromId(fromId);
            String toId = null;
            if(command == MessageCommand.MSG_P2P.getCommand()) {
                toId = obj.getString("toId");
            }else {
                toId = obj.getString("groupId");
            }
            request.setToId(toId);
            Result result = feignMessageService.checkMessage(request);
            if(result.isSuccess()) {
                MessageProducer.sendMessage(message, command);
            }else {
                sendValidationErrorMessage(ctx, command, obj, result);
            }
        } else {
            MessageProducer.sendMessage(message, command);
        }
    }

    private static void sendValidationErrorMessage(ChannelHandlerContext ctx, Integer command, JSONObject obj, Result result) {
        Integer ackCommand = 0;
        if(command == MessageCommand.MSG_P2P.getCommand()) {
            ackCommand = MessageCommand.MSG_ACK.getCommand();
        }else {
            ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
        }

        ChatMessageACK chatMessageACK = new ChatMessageACK(obj.getString("messageId"));
        result.setData(chatMessageACK);
        MessagePackage<Result> messagePackage = new MessagePackage<>();
        messagePackage.setData(result);
        messagePackage.setCommand(ackCommand);
        ctx.channel().writeAndFlush(messagePackage);
    }

    /**
     * 将UserSession存储到Redis
     * @param loginPackage
     * @param messageHeader
     * @param redissonClient
     * @throws UnknownHostException
     */
    private void cacheUserSession(LoginPackage loginPackage, MessageHeader messageHeader,
                           RedissonClient redissonClient) throws UnknownHostException {
        UserSession userSession = new UserSession();
        userSession.setAppId(messageHeader.getAppId());
        userSession.setClientType(messageHeader.getClientType());
        userSession.setImei(messageHeader.getImei());
        userSession.setUserId(loginPackage.getUserId());
        userSession.setConnectionState(ConnectionStatusEnum.ONLINE_STATUS.getCode());
        userSession.setBrokerId(brokerId);
        userSession.setBrokerHost(InetAddress.getLocalHost().getHostAddress());
        String sessionCacheKey = messageHeader.getAppId() + USER_SESSION + loginPackage.getUserId();
        RMap<String , String> map = redissonClient.getMap(sessionCacheKey);
        //key-field-value
        //key: appId:user-session:userId
        //field: clientType:imei
        //value: session
        map.put(messageHeader.getClientType() + ":" + messageHeader.getImei(), JSONObject.toJSONString(userSession));
    }

    /**
     * 向业务逻辑层发送用户上线通知消息
     * @param loginPackage
     * @param messageHeader
     */
    private static void sendUserOnlineStatusMessageToLogicModule(LoginPackage loginPackage, MessageHeader messageHeader) {
        UserStatusChangeNotifyPackage userStatusChangeNotifyPackage = new UserStatusChangeNotifyPackage();
        userStatusChangeNotifyPackage.setUserId(loginPackage.getUserId());
        userStatusChangeNotifyPackage.setAppId(messageHeader.getAppId());
        userStatusChangeNotifyPackage.setStatus(ConnectionStatusEnum.ONLINE_STATUS.getCode());
        MessageProducer.sendMessage(userStatusChangeNotifyPackage, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
    }


    /**
     * 通知用户登陆成功
     * @param ctx
     * @param loginPackage
     * @param messageHeader
     */
    private static void sendUserLoginSuccessACK(ChannelHandlerContext ctx, LoginPackage loginPackage, MessageHeader messageHeader) {
        MessagePackage<LoginACKPackage> loginSuccess = new MessagePackage<>();
        LoginACKPackage loginACKPackage = new LoginACKPackage();
        loginACKPackage.setUserId(loginPackage.getUserId());
        loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
        loginSuccess.setData(loginACKPackage);
        loginSuccess.setAppId(messageHeader.getAppId());
        loginSuccess.setImei(messageHeader.getImei());
        loginSuccess.setClientType(messageHeader.getClientType());
        ctx.channel().writeAndFlush(loginSuccess);
    }

    /**
     * 向其他Netty服务器广播该用户上线消息
     * @param loginPackage
     * @param messageHeader
     * @param redissonClient
     */
    private static void broadcastUserLoginMessage(LoginPackage loginPackage, MessageHeader messageHeader,
                                  RedissonClient redissonClient) {
        UserClientDTO userClientDTO = new UserClientDTO(
                messageHeader.getAppId(),
                messageHeader.getClientType(),
                loginPackage.getUserId(),
                messageHeader.getImei());
        RTopic topic = redissonClient.getTopic(RedisConstants.USER_LOGIN_CHANNEL);
        topic.publish(JSONObject.toJSONString(userClientDTO));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {


    }
}
