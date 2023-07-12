package com.paddi.service.module.user.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.user.model.UserStatusChangeNotifyContent;
import com.paddi.service.module.user.model.req.PullFriendOnlineStatusRequest;
import com.paddi.service.module.user.model.req.PullUserOnlineStatusRequest;
import com.paddi.service.module.user.model.req.SetUserCustomizedOnlineStatusRequest;
import com.paddi.service.module.user.model.req.SubscribeUserOnlineStatusRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 20:24:42
 */
public interface UserStatusService {
    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent userStatusChangeNotifyContent);

    Result subscribeUserOnlineStatus(SubscribeUserOnlineStatusRequest request);

    Result setUserCustomizedOnlineStatus(SetUserCustomizedOnlineStatusRequest request);

    Result getFriendOnlineStatus(PullFriendOnlineStatusRequest request);

    Result getUserOnlineStatus(PullUserOnlineStatusRequest request);
}
