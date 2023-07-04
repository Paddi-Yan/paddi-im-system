package com.paddi.service.module.friendship.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import com.paddi.service.module.friendship.model.req.ApproveFriendRequestReq;
import com.paddi.service.module.friendship.model.req.ReadFriendShipRequestReq;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 15:57:04
 */
public interface FriendShipRequestService {
    Result addFriendShipRequest(String fromId, FriendDTO toItem, Integer appId);

    Result approveFriendRequest(ApproveFriendRequestReq request);

    Result getFriendRequest(String fromId, Integer appId);

    Result readFriendShipRequestReq(ReadFriendShipRequestReq req);
}
