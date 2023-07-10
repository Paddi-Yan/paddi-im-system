package com.paddi.service.module.friendship.service;

import com.paddi.common.model.BaseRequest;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import com.paddi.service.module.friendship.model.req.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:33:50
 */
public interface FriendShipService {
    Result importFriendShip(ImportFriendShipRequest request);

    Result addFriend(AddFriendRequest request);

    Result updateFriend(UpdateFriendRequest request);

    Result doAddFriend(BaseRequest baseRequest, String fromId, FriendDTO toItem, Integer appId);

    Result deleteFriend(DeleteFriendRequest request);

    Result deleteAllFriend(DeleteFriendRequest request);

    Result getAllFriendShip(GetAllFriendShipRequest request);

    Result getRelation(GetRelationRequest request);

    Result checkFriendship(CheckFriendShipRequest request);

    Result addBlack(AddFriendShipBlackRequest request);

    Result deleteBlack(DeleteBlackRequest request);

    Result checkBlack(CheckFriendShipRequest request);

    Result syncFriendShipList(SyncRequest request);
}
