package com.paddi.service.module.group.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import com.paddi.service.module.group.entity.vo.GroupMemberVO;
import com.paddi.service.module.group.model.req.*;
import com.paddi.service.module.group.model.resp.GetMemberRoleResponse;

import java.util.Collection;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:08:34
 */
public interface GroupMemberService {
    Result importGroupMember(ImportGroupMemberRequest request);

    Result doAddGroupMember(String groupId, Integer appId, GroupMemberDTO member);

    Result<List<GroupMemberVO>> getGroupMember(String groupId, Integer appId);

    Result<GetMemberRoleResponse> getMemberRoleInGroup(String groupId, Integer appId, String memberId);

    Result<Collection<String>> getMemberJoinedGroup(GetJoinedGroupRequest request);

    Result transferGroupMember(String ownerId, String groupId, Integer appId);

    Result addMember(AddGroupMemberRequest request);

    Result removeMember(RemoveGroupMemberRequest request);

    Result doRemoveGroupMember(String groupId, Integer appId, String memberId);

    Result updateGroupMember(UpdateGroupMemberRequest request);
}
