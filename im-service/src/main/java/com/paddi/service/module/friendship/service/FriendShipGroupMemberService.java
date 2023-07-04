package com.paddi.service.module.friendship.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupMemberRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:35:56
 */
public interface FriendShipGroupMemberService {
    Result addGroupNumber(AddFriendShipGroupMemberRequest request);
    Result deleteGroupMember(DeleteFriendShipGroupMemberRequest request);

    int doAddGroupMember(Long groupId, String memberId);

    int clearGroupMember(Long groupId);

    int doDeleteGroupMember(Long groupId, String memberId);
}
