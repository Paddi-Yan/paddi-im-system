package com.paddi.service.module.friendship.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:36:13
 */
public interface FriendShipGroupService {
    Result addGroup(AddFriendShipGroupRequest request);

    Result getGroup(String fromId, String groupName, Integer appId);

    Result deleteGroup(DeleteFriendShipGroupRequest request);
}
