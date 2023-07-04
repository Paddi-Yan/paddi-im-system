package com.paddi.service.module.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.enums.FriendShipErrorCode;
import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.entity.po.FriendShipGroup;
import com.paddi.service.module.friendship.mapper.FriendShipGroupMapper;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupRequest;
import com.paddi.service.module.friendship.service.FriendShipGroupMemberService;
import com.paddi.service.module.friendship.service.FriendShipGroupService;
import com.paddi.service.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:36:22
 */
@Service
@Slf4j
public class FriendShipGroupServiceImpl implements FriendShipGroupService {

    @Autowired
    FriendShipGroupMapper friendShipGroupMapper;

    @Autowired
    private FriendShipGroupMemberService friendShipGroupMemberService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addGroup(AddFriendShipGroupRequest request) {
        LambdaQueryWrapper<FriendShipGroup> wrapper = Wrappers.lambdaQuery(FriendShipGroup.class)
                                                         .eq(FriendShipGroup :: getAppId, request.getAppId())
                                                         .eq(FriendShipGroup :: getFromId, request.getFromId())
                                                         .eq(FriendShipGroup :: getGroupName, request.getGroupName())
                                                         .eq(FriendShipGroup :: getDelFlag, DelFlagEnum.NORMAL.getCode());
        FriendShipGroup friendShipGroup = friendShipGroupMapper.selectOne(wrapper);
        if(friendShipGroup != null) {
            return Result.error(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }

        FriendShipGroup insertInfo = FriendShipGroup.builder()
                                               .appId(request.getAppId())
                                               .fromId(request.fromId)
                                               .groupName(request.getGroupName())
                                               .createTime(System.currentTimeMillis())
                                               .delFlag(DelFlagEnum.NORMAL.getCode()).build();
        try {
            int insert = friendShipGroupMapper.insert(insertInfo);
            if(insert != 1) {
                return Result.error(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
            }
            if(insert == 1 && CollectionUtil.isNotEmpty(request.getToIds())) {
                AddFriendShipGroupMemberRequest addFriendShipGroupMemberRequest = new AddFriendShipGroupMemberRequest();
                addFriendShipGroupMemberRequest.setFromId(request.fromId);
                addFriendShipGroupMemberRequest.setGroupName(request.getGroupName());
                addFriendShipGroupMemberRequest.setToIds(request.getToIds());
                addFriendShipGroupMemberRequest.setAppId(request.getAppId());
                friendShipGroupMemberService.addGroupNumber(addFriendShipGroupMemberRequest);
            }
        } catch(DuplicateKeyException e) {
            log.error(e.getMessage());
            return Result.error(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }
        return Result.success();
    }

    @Override
    public Result getGroup(String fromId, String groupName, Integer appId) {
        FriendShipGroup friendShipGroup = friendShipGroupMapper.selectOne(Wrappers.lambdaQuery(FriendShipGroup.class)
                                                                                  .eq(FriendShipGroup :: getFromId, fromId)
                                                                                  .eq(FriendShipGroup :: getGroupName, groupName)
                                                                                  .eq(FriendShipGroup :: getAppId, appId)
                                                                                  .eq(FriendShipGroup :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
        if(friendShipGroup == null) {
            return Result.error(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }
        return Result.success(friendShipGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteGroup(DeleteFriendShipGroupRequest request) {
        for(String groupName : request.getGroupName()) {
            LambdaQueryWrapper<FriendShipGroup> wrapper = Wrappers.lambdaQuery(FriendShipGroup.class)
                                                             .eq(FriendShipGroup :: getAppId, request.getAppId())
                                                             .eq(FriendShipGroup :: getGroupName, groupName)
                                                             .eq(FriendShipGroup :: getFromId, request.getFromId())
                                                             .eq(FriendShipGroup :: getDelFlag, DelFlagEnum.NORMAL.getCode());
            FriendShipGroup friendShipGroup = friendShipGroupMapper.selectOne(wrapper);
            if(friendShipGroup != null) {
                FriendShipGroup deleteInfo = new FriendShipGroup();
                deleteInfo.setGroupId(friendShipGroup.getGroupId());
                deleteInfo.setDelFlag(DelFlagEnum.DELETE.getCode());
                friendShipGroupMapper.updateById(deleteInfo);
                friendShipGroupMemberService.clearGroupMember(friendShipGroup.getGroupId());
            }
        }
        return Result.success();
    }
}
