package com.paddi.service.module.friendship.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.entity.po.FriendShipGroup;
import com.paddi.service.module.friendship.entity.po.FriendShipGroupMember;
import com.paddi.service.module.friendship.mapper.FriendShipGroupMemberMapper;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.service.FriendShipGroupMemberService;
import com.paddi.service.module.friendship.service.FriendShipGroupService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:36:03
 */
@Service
@Slf4j
public class FriendShipGroupMemberServiceImpl implements FriendShipGroupMemberService {

    @Autowired
    private FriendShipGroupService friendShipGroupService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendShipGroupMemberMapper friendShipGroupMemberMapper;

    @Autowired
    private FriendShipGroupMemberService selfService;

    @Override
    @Transactional
    public Result addGroupNumber(AddFriendShipGroupMemberRequest request) {
        Result<FriendShipGroup> groupQueryResult = friendShipGroupService.getGroup(request.getFromId(), request.getGroupName(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        List<String> successId = new ArrayList<>();
        for(String memberId : request.getToIds()) {
            int res = selfService.doAddGroupMember(groupQueryResult.getData().getGroupId(), memberId);
            if(res == 1) {
                successId.add(memberId);
            }
        }
        return Result.success(successId);
    }

    @Override
    public int doAddGroupMember(Long groupId, String memberId) {
        FriendShipGroupMember member = new FriendShipGroupMember();
        member.setGroupId(groupId);
        member.setToId(memberId);
        try {
            return friendShipGroupMemberMapper.insert(member);
        } catch(Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public int clearGroupMember(Long groupId) {
        return friendShipGroupMemberMapper.delete(Wrappers.lambdaQuery(FriendShipGroupMember.class)
                                                          .eq(FriendShipGroupMember :: getGroupId, groupId));
    }


    @Override
    public Result deleteGroupMember(DeleteFriendShipGroupMemberRequest request) {
        Result<FriendShipGroup> groupQueryResult = friendShipGroupService.getGroup(request.getFromId(), request.getGroupName(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        List<String> successId = new ArrayList<>();
        for(String memberId : request.getToIds()) {
            Result<User> memberInfo = userService.getSingleUserInfo(memberId, request.getAppId());
            if(memberInfo.isSuccess()) {
                int res = selfService.doDeleteGroupMember(groupQueryResult.getData().getGroupId(), memberId);
                if(res == 1) {
                    successId.add(memberId);
                }
            }
        }
        return Result.success(successId);
    }

    @Override
    public int doDeleteGroupMember(Long groupId, String memberId) {
        try {
            return friendShipGroupMemberMapper.delete(
                    Wrappers.lambdaQuery(FriendShipGroupMember.class)
                            .eq(FriendShipGroupMember::getGroupId, groupId)
                            .eq(FriendShipGroupMember::getToId, memberId));
        } catch(Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

}
