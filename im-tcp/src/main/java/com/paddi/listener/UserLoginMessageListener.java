package com.paddi.listener;

import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.model.UserClientDTO;
import com.paddi.factory.MultiTerminalLoginProcessStrategyFactory;
import com.paddi.redis.RedisManager;
import com.paddi.strategy.multi.MultiTerminalLoginProcessStrategy;
import com.paddi.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.List;

/**
 * 多端同步模式：1 单端登录：只允许一端在线，手机/电脑/web 踢掉除了本client+imei以外的设备
 *             2 双端登录：允许手机/电脑的一台设备 + web在线 踢掉除了本client+imei的非web端设备
 *             3 三段登录：允许手机和电脑单设备 + web同时在线 踢掉非本client+imei的同端设备
 *             4 多端登录：允许所有端多设备登录 不踢任何设备
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 00:51:50
 */
@Slf4j
public class UserLoginMessageListener {

    private Integer loginModel;

    private final MultiTerminalLoginProcessStrategy strategy;


    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
        strategy = MultiTerminalLoginProcessStrategyFactory.getInstance(loginModel);
    }

    public void listenUserLogin() {
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RTopic topic = redissonClient.getTopic(Constants.RedisConstants.USER_LOGIN_CHANNEL);
        topic.addListener(String.class, (charSequence, message) -> {
            log.info("收到用户上线: [{}]", message);
            UserClientDTO userClientDTO = JSONObject.parseObject(message, UserClientDTO.class);
            List<NioSocketChannel> channels = SessionSocketHolder.get(userClientDTO.getAppId(), userClientDTO.getUserId());
            strategy.process(channels, userClientDTO);
        });
    }
}
