package com.paddi.service.module.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.GroupErrorCode;
import com.paddi.common.enums.GroupMemberRoleEnum;
import com.paddi.common.enums.GroupStatusEnum;
import com.paddi.common.enums.GroupTypeEnum;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.exception.BadRequestException;
import com.paddi.common.model.Result;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import com.paddi.service.module.group.entity.po.Group;
import com.paddi.service.module.group.entity.vo.GroupMemberVO;
import com.paddi.service.module.group.mapper.GroupMapper;
import com.paddi.service.module.group.model.req.*;
import com.paddi.service.module.group.model.resp.GetGroupResponse;
import com.paddi.service.module.group.model.resp.GetJoinedGroupResponse;
import com.paddi.service.module.group.model.resp.GetMemberRoleResponse;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.group.service.GroupService;
import com.paddi.service.utils.CallbackService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:09:42
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private CallbackService callbackService;


    @Override
    public Result importGroup(ImportGroupRequest request) {
        if(StrUtil.isEmpty(request.getGroupId())) {
            request.setGroupId(IdUtil.simpleUUID());
        }else {
            Integer count = groupMapper.selectCount(Wrappers.lambdaQuery(Group.class)
                                                            .eq(Group :: getAppId, request.getAppId())
                                                            .eq(Group :: getGroupId, request.getGroupId()));
            if(count > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }
        Group group = new Group();
        if(request.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StrUtil.isEmpty(request.getOwnerId())) {
            throw new BadRequestException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }
        if(request.getCreateTime() == null) {
            group.setCreateTime(System.currentTimeMillis());
        }
        group.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(request, group);
        int res = groupMapper.insert(group);
        if(res != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result<Group> getGroup(String groupId, Integer appId) {
        Group group = groupMapper.selectOne(Wrappers.lambdaQuery(Group.class)
                                                    .eq(Group :: getAppId, appId)
                                                    .eq(Group :: getGroupId, groupId));
        if(group == null) {
            return Result.error(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return Result.success(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createGroup(CreateGroupRequest request) {
        boolean isAmin = false;

        if(!isAmin) {
            request.setOwnerId(request.getOperator());
        }
        if(StrUtil.isEmpty(request.getGroupId())) {
            request.setGroupId(IdUtil.simpleUUID());
        }else {
            Integer count = groupMapper.selectCount(Wrappers.lambdaQuery(Group.class)
                                                            .eq(Group :: getAppId, request.getAppId())
                                                            .eq(Group :: getGroupId, request.getGroupId()));
            if(count > 0) {
                throw new BadRequestException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if(request.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StrUtil.isEmpty(request.getOwnerId())) {
            throw new BadRequestException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        if(configuration.isCreateGroupBeforeCallback()) {
            Result result = callbackService.callbackSync(request.getAppId(),
                    Constants.CallbackCommand.CreateGroupBefore,
                    JSONObject.toJSONString(request));
            if(!result.isSuccess()) {
                return result;
            }
        }

        Group group = new Group();
        group.setCreateTime(System.currentTimeMillis());
        group.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(request, group);
        groupMapper.insert(group);

        GroupMemberDTO groupOwner = new GroupMemberDTO();
        groupOwner.setMemberId(request.getOwnerId());
        groupOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupOwner.setJoinTime(System.currentTimeMillis());
        //插入群主信息
        groupMemberService.doAddGroupMember(request.getGroupId(), request.getAppId(), groupOwner);
        //插入群成员信息
        for(GroupMemberDTO groupMember : request.getMember()) {
            groupMemberService.doAddGroupMember(request.getGroupId(), request.getAppId(), groupMember);
        }

        if(configuration.isCreateGroupAfterCallback()) {
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.CreateGroupAfter,
                    JSONObject.toJSONString(group));
        }
        return Result.success();
    }

    @Override
    public Result getGroup(GetGroupRequest request) {
        Result<Group> groupQueryResult = getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        GetGroupResponse response = new GetGroupResponse();
        BeanUtils.copyProperties(groupQueryResult.getData(), response);
        Result<List<GroupMemberVO>> groupMemberQueryResult = groupMemberService.getGroupMember(request.getGroupId(), request.getAppId());
        if(groupMemberQueryResult.isSuccess()) {
            response.setMember(groupMemberQueryResult.getData());
        }
        return Result.success(response);
    }

    /**
     * 修改群基础信息
     * 如果是后台管理员调用则不检查权限 如果不是则检查权限
     * 如果是私有群（微信群）任何人都可以修改资料，公开群只有管理员可以修改
     * 如果是群主或者管理员可以修改其他信息。
     * @param request
     * @return
     */
    @Override
    public Result updateBaseGroupInfo(UpdateGroupRequest request) {
        LambdaQueryWrapper<Group> wrapper = Wrappers.lambdaQuery(Group.class)
                                               .eq(Group :: getGroupId, request.getGroupId())
                                               .eq(Group :: getAppId, request.getAppId());
        Group group = groupMapper.selectOne(wrapper);
        checkGroup(group);
        boolean isAdmin = false;

        if(!isAdmin) {
            Result<GetMemberRoleResponse> memberRoleQueryResult = groupMemberService.getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getOperator());
            if(!memberRoleQueryResult.isSuccess()) {
                return memberRoleQueryResult;
            }

            GetMemberRoleResponse memberRole = memberRoleQueryResult.getData();
            Integer role = memberRole.getRole();
            boolean isManager = GroupMemberRoleEnum.MAMAGER.getCode() == role || GroupMemberRoleEnum.OWNER.getCode() == role;

            if(!isManager && GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
                throw new BadRequestException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        Group updateInfo = new Group();
        BeanUtils.copyProperties(request, updateInfo);
        updateInfo.setUpdateTime(System.currentTimeMillis());
        int res = groupMapper.update(updateInfo, wrapper);
        if(res != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result getJoinedGroup(GetJoinedGroupRequest request) {
        Result<Collection<String>> memberJoinedGroupQueryResult = groupMemberService.getMemberJoinedGroup(request);
        if(memberJoinedGroupQueryResult.isSuccess()) {
            GetJoinedGroupResponse response = new GetJoinedGroupResponse();
            Collection<String> groupIdList = memberJoinedGroupQueryResult.getData();
            if(CollectionUtil.isEmpty(groupIdList)) {
                response.setTotalCount(0);
                response.setGroupList(new ArrayList<>());
                return Result.success(response);
            }
            LambdaQueryWrapper<Group> wrapper = Wrappers.lambdaQuery(Group.class)
                                                   .eq(Group :: getAppId, request.getAppId())
                                                   .in(Group :: getGroupId, groupIdList);
            if(CollectionUtil.isNotEmpty(request.getGroupType())) {
                wrapper.in(Group::getGroupType, request.getGroupType());
            }
            List<Group> groupList = groupMapper.selectList(wrapper);
            response.setGroupList(groupList);
            if(request.getPageParam() == null || request.getPageParam().getPageNum() == null || request.getPageParam().getPageSize() == null) {
                response.setTotalCount(groupList.size());
            }else {
                response.setTotalCount(groupMapper.selectCount(wrapper));
            }
            return Result.success(response);
        }
        return memberJoinedGroupQueryResult;
    }

    @Override
    public Result destroyGroup(DestroyGroupRequest request) {
        boolean isAdmin  = false;
        LambdaQueryWrapper<Group> wrapper = Wrappers.lambdaQuery(Group.class)
                                               .eq(Group :: getGroupId, request.getGroupId())
                                               .eq(Group :: getAppId, request.getAppId());
        Group group = groupMapper.selectOne(wrapper);
        checkGroup(group);
        if(!isAdmin) {
            if(group.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
                throw new BadRequestException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTORY);
            }
            if(group.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && !group.getOwnerId().equals(request.getOperator())) {
                throw new BadRequestException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        Group updateInfo = new Group();
        updateInfo.setStatus(GroupStatusEnum.DESTROY.getCode());
        int res = groupMapper.update(updateInfo, wrapper);
        if(res != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result transferGroup(TransferGroupRequest request) {
        LambdaQueryWrapper<Group> wrapper = Wrappers.lambdaQuery(Group.class)
                                               .eq(Group :: getAppId, request.getAppId())
                                               .eq(Group :: getGroupId, request.getGroupId());
        Group group = groupMapper.selectOne(wrapper);
        checkGroup(group);
        Result<GetMemberRoleResponse> memberRoleQueryResult = groupMemberService.getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getOperator());
        if(!memberRoleQueryResult.isSuccess()) {
            return memberRoleQueryResult;
        }

        if(memberRoleQueryResult.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return Result.error(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        Result<GetMemberRoleResponse> newOwnerRoleQueryResult = groupMemberService.getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getOwnerId());
        if(!newOwnerRoleQueryResult.isSuccess()) {
            return newOwnerRoleQueryResult;
        }

        Group updateInfo = new Group();
        updateInfo.setOwnerId(request.getOwnerId());
        groupMapper.update(updateInfo, wrapper);
        groupMemberService.transferGroupMember(request.getOwnerId(), request.getGroupId(), request.getAppId());
        return Result.success();
    }

    @Override
    public Result muteGroup(MuteGroupRequest request) {
        Result<Group> groupQueryResult = getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }

        if(groupQueryResult.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;

        if(!isAdmin) {
            Result<GetMemberRoleResponse> memberRoleQueryResult = groupMemberService.getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getOperator());
            if(!memberRoleQueryResult.isSuccess()) {
                return memberRoleQueryResult;
            }
            GetMemberRoleResponse data = memberRoleQueryResult.getData();
            Integer role = data.getRole();
            boolean isManager = role == GroupMemberRoleEnum.OWNER.getCode() || role == GroupMemberRoleEnum.MAMAGER.getCode();
            if(!isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        Group updateGroup = new Group();
        updateGroup.setMute(request.getMute());
        groupMapper.update(updateGroup, Wrappers.lambdaUpdate(Group.class)
                .eq(Group::getAppId, request.getAppId())
                .eq(Group::getGroupId, request.getGroupId()));
        return Result.success();
    }

    private void checkGroup(Group group) {
        if(group == null) {
            throw new BadRequestException(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        if(group.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new BadRequestException(GroupErrorCode.GROUP_IS_DESTROY);
        }
    }
}
