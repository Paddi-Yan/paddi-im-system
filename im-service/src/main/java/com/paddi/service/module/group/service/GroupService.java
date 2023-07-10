package com.paddi.service.module.group.service;

import com.paddi.common.model.Result;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.service.module.group.entity.po.Group;
import com.paddi.service.module.group.model.req.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:09:38
 */
public interface GroupService {
    Result importGroup(ImportGroupRequest request);

    Result<Group> getGroup(String groupId, Integer appId);

    Result createGroup(CreateGroupRequest request);

    Result getGroup(GetGroupRequest request);

    Result updateBaseGroupInfo(UpdateGroupRequest request);

    Result getJoinedGroup(GetJoinedGroupRequest request);

    Result destroyGroup(DestroyGroupRequest request);

    Result transferGroup(TransferGroupRequest request);

    Result muteGroup(MuteGroupRequest request);

    Result syncJoinedGroupList(SyncRequest request);

    Long getGroupMaxSequence(Integer appId, String userId);
}
