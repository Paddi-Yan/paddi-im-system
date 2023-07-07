package com.paddi.service.module.message.service.impl;

import com.paddi.common.enums.*;
import com.paddi.common.model.Result;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.friendship.entity.po.FriendShip;
import com.paddi.service.module.friendship.model.req.GetRelationRequest;
import com.paddi.service.module.friendship.service.FriendShipService;
import com.paddi.service.module.group.entity.po.Group;
import com.paddi.service.module.group.model.resp.GetMemberRoleResponse;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.group.service.GroupService;
import com.paddi.service.module.message.service.CheckSendMessageService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:04:57
 */
@Service
public class CheckSendMessageServiceImpl implements CheckSendMessageService {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendShipService friendShipService;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Override
    public Result checkSenderForbidOrMute(String fromId, Integer appId) {
        Result<User> userInfoQueryResult = userService.getSingleUserInfo(fromId, appId);
        if(!userInfoQueryResult.isSuccess()) {
            return userInfoQueryResult;
        }
        User user = userInfoQueryResult.getData();
        if(user.getForbiddenFlag().equals(UserForbiddenFlagEnum.FORBIBBEN.getCode())) {

            return Result.error(MessageErrorCode.FROMER_IS_FORBIBBEN);
        }else if(user.getSilentFlag().equals(UserSilentFlagEnum.MUTE.getCode())) {
            return Result.error(MessageErrorCode.FROMER_IS_MUTE);
        }
        return Result.success();
    }

    @Override
    public Result checkFriendShip(String fromId, String toId, Integer appId) {
        //1.校验好友关系和是否拉黑
        //2.校验好友关系
        //3.校验是否拉黑
        //4.什么都不用校验
        if(configuration.isSendMessageCheckFriend() || configuration.isSendMessageCheckBlack()) {
            GetRelationRequest fromReq = new GetRelationRequest();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            Result<FriendShip> fromRelation = friendShipService.getRelation(fromReq);
            if(!fromRelation.isSuccess()) {
                return fromRelation;
            }
            GetRelationRequest toReq = new GetRelationRequest();
            toReq.setFromId(fromId);
            toReq.setToId(toId);
            toReq.setAppId(appId);
            Result<FriendShip> toRelation = friendShipService.getRelation(toReq);
            if(!toRelation.isSuccess()) {
                return toRelation;
            }

            if(configuration.isSendMessageCheckFriend()) {
                if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromRelation.getData().getStatus()) {
                    return Result.error(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
                }
                if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toRelation.getData().getStatus()) {
                    return Result.error(FriendShipErrorCode.FRIEND_IS_DELETED);
                }
            }

            if(configuration.isSendMessageCheckBlack()) {
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != fromRelation.getData().getBlack()) {
                    return Result.error(FriendShipErrorCode.FRIEND_IS_BLACK);
                }
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != toRelation.getData().getBlack()) {
                    return Result.error(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result checkGroupMessage(String fromId, String groupId, Integer appId) {
        Result result = checkSenderForbidOrMute(fromId, appId);
        if(!result.isSuccess()) {
            return result;
        }
        Result<Group> groupQueryResult = groupService.getGroup(groupId, appId);
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        //判断是否在群内
        Result<GetMemberRoleResponse> memberStateResult = groupMemberService.getMemberRoleInGroup(groupId, appId, fromId);
        if(!memberStateResult.isSuccess()) {
            return memberStateResult;
        }
        //判断群聊状态
        //如果群聊状态处于禁言状态-只有管理员和群主可以发言
        Group group = groupQueryResult.getData();
        GetMemberRoleResponse memberRole = memberStateResult.getData();
        if(group.getMute().equals(GroupMuteTypeEnum.MUTE.getCode())
                && memberRole.getRole().equals(GroupMemberRoleEnum.ORDINARY.getCode())) {
            //群聊被禁言并且发送消息者的身份是普通成员
            return Result.error(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }
        if(memberRole.getSpeakDate() != null && memberRole.getSpeakDate() > System.currentTimeMillis()) {
            return Result.error(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return Result.success();
    }
}
