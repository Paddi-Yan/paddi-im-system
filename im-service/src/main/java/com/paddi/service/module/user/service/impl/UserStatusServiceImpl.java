package com.paddi.service.module.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.codec.pack.user.UserCustomizedStatusChangeNotifyPackage;
import com.paddi.codec.pack.user.UserStatusChangeNotifyPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.command.UserEventCommand;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.UserSession;
import com.paddi.service.module.friendship.service.FriendShipService;
import com.paddi.service.module.user.entity.po.UserCustomizedOnlineStatus;
import com.paddi.service.module.user.model.UserStatusChangeNotifyContent;
import com.paddi.service.module.user.model.req.PullFriendOnlineStatusRequest;
import com.paddi.service.module.user.model.req.PullUserOnlineStatusRequest;
import com.paddi.service.module.user.model.req.SetUserCustomizedOnlineStatusRequest;
import com.paddi.service.module.user.model.req.SubscribeUserOnlineStatusRequest;
import com.paddi.service.module.user.model.resp.PullUserOnlineStatusResponse;
import com.paddi.service.module.user.service.UserStatusService;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.UserSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 20:24:49
 */
@Service
public class UserStatusServiceImpl implements UserStatusService {

    @Autowired
    private UserSessionUtils userSessionUtils;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private FriendShipService friendShipService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {
        List<UserSession> userSessionList = userSessionUtils.getAllTerminalUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPackage notifyPackage = new UserStatusChangeNotifyPackage();
        BeanUtils.copyProperties(content, notifyPackage);
        //notifyPackage.setClient(userSessionList);

        //发送给当前用户的在线同步端
        syncToUser(notifyPackage, content.getUserId(), new ClientInfo(content.getAppId(), content.getClientType(), content.getImei()));

        //发送给在线好友
        dispatchToOnlineFriend(notifyPackage, content.getUserId(), content.getAppId());

        //发送给订阅了当前用户的在线端
        dispatchToSubscribedUser(notifyPackage, content.getUserId(), content.getAppId());
    }

    /**
     * 订阅用户的在线状态
     * 使用Redis的Hash结构存储每个用户的被订阅列表
     * 该列表用于存储哪些用户订阅了自己的在线状态以及其订阅过期时间
     * Hash
     * A - [B:time, C:time]
     * @param request
     * @return
     */
    @Override
    public Result subscribeUserOnlineStatus(SubscribeUserOnlineStatusRequest request) {
        Long subscribeExpireTime = 0L;
        if(request != null && request.getSubscribeTime() > 0) {
            subscribeExpireTime = System.currentTimeMillis() + request.getSubscribeTime();
        }
        for(String userId : request.getSubscribedUserId()) {
            String hashKey = request.getAppId() + Constants.RedisConstants.SUBSCRIBE + userId;
            redisTemplate.opsForHash().put(hashKey, request.getOperator(), subscribeExpireTime.toString());
        }
        return Result.success();
    }

    @Override
    public Result setUserCustomizedOnlineStatus(SetUserCustomizedOnlineStatusRequest request) {
        UserCustomizedOnlineStatus onlineStatus = new UserCustomizedOnlineStatus(request.getCustomizedText(), request.getCustomizedStatus());

        String key = request.getAppId() + Constants.RedisConstants.USER_CUSTOMIZED_STATUS + request.getUserId();
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(onlineStatus));

        UserCustomizedStatusChangeNotifyPackage notifyPackage = new UserCustomizedStatusChangeNotifyPackage();
        BeanUtils.copyProperties(onlineStatus, notifyPackage);
        notifyPackage.setUserId(request.getUserId());

        syncToUser(notifyPackage, request.getUserId(), new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

        dispatchToOnlineFriend(notifyPackage, request.getUserId(), request.getAppId());

        dispatchToSubscribedUser(notifyPackage, request.getUserId(), request.getAppId());

        return Result.success();
    }

    @Override
    public Result getFriendOnlineStatus(PullFriendOnlineStatusRequest request) {
        List<String> friendList = friendShipService.getFriendList(request.getAppId(), request.getOperator());
        return Result.success(getUserOnlineStatus(request.getAppId(), friendList));
    }

    @Override
    public Result getUserOnlineStatus(PullUserOnlineStatusRequest request) {
        return Result.success(getUserOnlineStatus(request.getAppId(), request.getUserList()));
    }

    private Map<String, PullUserOnlineStatusResponse> getUserOnlineStatus(Integer appId, List<String> userList) {
        Map<String, PullUserOnlineStatusResponse> result = new HashMap<>(userList.size());
        for(String userId : userList) {
            PullUserOnlineStatusResponse response = new PullUserOnlineStatusResponse();
            List<UserSession> sessions = userSessionUtils.getAllTerminalUserSession(appId, userId);
            response.setSessions(sessions);

            String key = appId + Constants.RedisConstants.USER_CUSTOMIZED_STATUS + userId;
            String str = redisTemplate.opsForValue().get(key);
            if(StrUtil.isNotEmpty(str)) {
                UserCustomizedOnlineStatus status = JSONObject.parseObject(str, UserCustomizedOnlineStatus.class);
                response.setCustomizedStatus(status.getCustomizedStatus());
                response.setCustomizedText(status.getCustomizedText());
            }
            result.put(userId, response);
        }
        return result;
    }

    private void syncToUser(Object notifyPackage, String userId, ClientInfo clientInfo) {
        messageProducer.sendToOtherUserTerminal(userId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                notifyPackage, clientInfo);
    }

    private void dispatchToOnlineFriend(Object notifyPackage, String userId, Integer appId) {
        List<String> friendList = friendShipService.getFriendList(appId, userId);
        //发送给在线好友该用户上线状态通知
        for(String friendId : friendList) {
            messageProducer.sendToAllUserTerminal(friendId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    notifyPackage, appId);
        }
    }

    private void dispatchToSubscribedUser(Object notifyPackage, String userId, Integer appId) {
        //发送给订阅了该用户在线状态通知的用户
        String hashKey = appId + Constants.RedisConstants.SUBSCRIBE + userId;
        Map<Object, Object> subscribedUserMap = redisTemplate.opsForHash().entries(hashKey);
        for(Map.Entry<Object, Object> entry : subscribedUserMap.entrySet()) {
            String subscribeUserId = String.valueOf(entry.getKey());
            Long subscribeExpiredTime = Long.valueOf(String.valueOf(entry.getValue()));
            //订阅时间未过期
            if(subscribeExpiredTime > System.currentTimeMillis()) {
                messageProducer.sendToAllUserTerminal(subscribeUserId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        notifyPackage, appId);
            }else {
                redisTemplate.opsForHash().delete(hashKey, subscribeUserId);
            }
        }
    }
}
