package com.paddi.service.module.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.group.*;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.GroupErrorCode;
import com.paddi.common.enums.GroupMemberRoleEnum;
import com.paddi.common.enums.GroupStatusEnum;
import com.paddi.common.enums.GroupTypeEnum;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.exception.BadRequestException;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.common.model.message.SyncResponse;
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
import com.paddi.service.utils.GroupMessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
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

    @Autowired
    private GroupMessageProducer groupMessageProducer;

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;


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

        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Group);

        Group group = new Group();
        group.setCreateTime(System.currentTimeMillis());
        group.setStatus(GroupStatusEnum.NORMAL.getCode());
        group.setSequence(sequence);
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

        CreateGroupPackage createGroupPackage = new CreateGroupPackage();
        createGroupPackage.setSequence(sequence);
        BeanUtils.copyProperties(group, createGroupPackage);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.CREATED_GROUP, createGroupPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

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
        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Group);
        Group updateInfo = new Group();
        BeanUtils.copyProperties(request, updateInfo);
        updateInfo.setUpdateTime(System.currentTimeMillis());
        updateInfo.setSequence(sequence);
        int res = groupMapper.update(updateInfo, wrapper);
        if(res != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        if(configuration.isModifyGroupAfterCallback()) {
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.UpdateGroupAfter,
                    JSONObject.toJSONString(request));
        }

        UpdateGroupInfoPackage updateGroupInfoPackage = new UpdateGroupInfoPackage();
        BeanUtils.copyProperties(request, updateGroupInfoPackage);
        updateGroupInfoPackage.setSequence(sequence);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.UPDATED_GROUP, updateGroupInfoPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

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
        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Group);
        Group updateInfo = new Group();
        updateInfo.setSequence(sequence);
        updateInfo.setStatus(GroupStatusEnum.DESTROY.getCode());
        int res = groupMapper.update(updateInfo, wrapper);
        if(res != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        if(configuration.isDestroyGroupAfterCallback()) {
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.DestroyGroupAfter,
                    JSONObject.toJSONString(request));
        }

        DestroyGroupPackage destroyGroupPackage = new DestroyGroupPackage();
        destroyGroupPackage.setGroupId(request.getGroupId());
        destroyGroupPackage.setSequence(sequence);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.DESTROY_GROUP, destroyGroupPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

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
        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Group);

        Group updateInfo = new Group();
        updateInfo.setOwnerId(request.getOwnerId());
        updateInfo.setSequence(sequence);
        groupMapper.update(updateInfo, wrapper);
        groupMemberService.transferGroupMember(request.getOwnerId(), request.getGroupId(), request.getAppId());

        if(configuration.isTransferGroupAfterCallback()) {
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.TransferGroupAfter,
                    JSONObject.toJSONString(request));
        }

        TransferGroupPackage transferGroupPackage = new TransferGroupPackage();
        transferGroupPackage.setGroupId(request.getGroupId());
        transferGroupPackage.setOwnerId(request.getOwnerId());
        transferGroupPackage.setSequence(sequence);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.TRANSFER_GROUP, transferGroupPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));
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
        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Group);
        Group updateGroup = new Group();
        updateGroup.setSequence(sequence);
        updateGroup.setMute(request.getMute());
        groupMapper.update(updateGroup, Wrappers.lambdaUpdate(Group.class)
                .eq(Group::getAppId, request.getAppId())
                .eq(Group::getGroupId, request.getGroupId()));

        MuteGroupPackage muteGroupPackage = new MuteGroupPackage();
        muteGroupPackage.setSequence(sequence);
        muteGroupPackage.setGroupId(request.getGroupId());
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.MUTE_GROUP, muteGroupPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

        return Result.success();
    }

    @Override
    public Result syncJoinedGroupList(SyncRequest request) {
        if(request.getLimit() > 100) {
            request.setLimit(100);
        }

        SyncResponse<Group> syncResponse = new SyncResponse<>();
        List<String> joinedGroupIdList = groupMemberService.getJoinedGroupIdList(request.getAppId(), request.getOperator());

        if(CollectionUtil.isNotEmpty(joinedGroupIdList)) {
            List<Group> groupList = groupMapper.selectList(Wrappers.lambdaQuery(Group.class)
                                                                .eq(Group :: getAppId, request.getAppId())
                                                                .in(Group :: getGroupId, joinedGroupIdList)
                                                                .gt(Group :: getSequence, request.getLastSequence())
                                                                .last("limit" + request.getLimit())
                                                                .orderByAsc(Group :: getSequence));
            if(CollectionUtil.isNotEmpty(groupList)) {
                Group maxSequenceGroup = groupList.get(groupList.size() - 1);
                Long maxSequence = groupMapper.getMaxSequence(request.getAppId(), joinedGroupIdList);
                syncResponse.setDataList(groupList);
                syncResponse.setMaxSequence(maxSequence);
                syncResponse.setIsCompleted(maxSequenceGroup.getSequence() >= maxSequence);
                return Result.success(syncResponse);
            }
        }
        syncResponse.setIsCompleted(true);
        return Result.success(syncResponse);
    }

    @Override
    public Long getGroupMaxSequence(Integer appId, String userId) {
        List<String> joinedGroupIdList = groupMemberService.getJoinedGroupIdList(appId, userId);
        if(CollectionUtil.isNotEmpty(joinedGroupIdList)) {
            Long maxSequence = groupMapper.getMaxSequence(appId, joinedGroupIdList);
            return maxSequence;
        }
        return 0L;
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
