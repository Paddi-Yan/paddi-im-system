package com.paddi.service.module.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.paddi.codec.pack.group.AddGroupMemberPackage;
import com.paddi.codec.pack.group.RemoveGroupMemberPackage;
import com.paddi.codec.pack.group.UpdateGroupMemberPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.GroupErrorCode;
import com.paddi.common.enums.GroupMemberRoleEnum;
import com.paddi.common.enums.GroupStatusEnum;
import com.paddi.common.enums.GroupTypeEnum;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.exception.BadRequestException;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.PageParam;
import com.paddi.common.model.Result;
import com.paddi.common.utils.PageUtils;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import com.paddi.service.module.group.entity.po.Group;
import com.paddi.service.module.group.entity.po.GroupMember;
import com.paddi.service.module.group.entity.vo.GroupMemberVO;
import com.paddi.service.module.group.mapper.GroupMemberMapper;
import com.paddi.service.module.group.model.callback.AddGroupMemberCallbackRequest;
import com.paddi.service.module.group.model.req.*;
import com.paddi.service.module.group.model.resp.AddGroupMemberResponse;
import com.paddi.service.module.group.model.resp.GetMemberRoleResponse;
import com.paddi.service.module.group.model.resp.ImportGroupMemberResponse;
import com.paddi.service.module.group.service.GroupMemberService;
import com.paddi.service.module.group.service.GroupService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.service.UserService;
import com.paddi.service.utils.CallbackService;
import com.paddi.service.utils.GroupMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:08:46
 */
@Service
@Slf4j
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private GroupService groupService;

    @Autowired
    GroupMemberService selfService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private CallbackService callbackService;

    @Autowired
    private GroupMessageProducer groupMessageProducer;

    @Override
    public Result importGroupMember(ImportGroupMemberRequest request) {
        List<ImportGroupMemberResponse> responses = new ArrayList<>();
        Result<Group> groupQueryResult = groupService.getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        List<GroupMemberDTO> members = request.getMembers();

        if(configuration.isAddGroupMemberBeforeCallback()) {
            Result result = callbackService.callbackSync(request.getAppId(),
                    Constants.CallbackCommand.GroupMemberAddBefore,
                    JSONObject.toJSONString(request));
            if(!result.isSuccess()) {
                return result;
            }
            try {
                members = JSONArray.parseArray(JSONObject.toJSONString(result.getData()), GroupMemberDTO.class);
            } catch(Exception e) {
                log.error("添加群成员[{}]回调失败, error=[{}]", request, e.getMessage());
            }
        }

        for(GroupMemberDTO member : members) {
            Result result = selfService.doAddGroupMember(request.getGroupId(), request.getAppId(), member);
            ImportGroupMemberResponse response = new ImportGroupMemberResponse();
            response.setMemberId(member.getMemberId());
            if(result.isSuccess()) {
                response.setResult(0);
            } else if(result.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                response.setResult(2);
            }else {
                response.setResult(1);
            }
            responses.add(response);
        }

        if(configuration.isAddGroupMemberAfterCallback()) {
            AddGroupMemberCallbackRequest callbackRequest = new AddGroupMemberCallbackRequest();
            callbackRequest.setGroupId(request.getGroupId());
            callbackRequest.setOperator(request.getOperator());
            callbackRequest.setGroupType(groupQueryResult.getData().getGroupType());
            callbackRequest.setMembers(members);
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.GroupMemberAddAfter,
                    JSONObject.toJSONString(callbackRequest));
        }

        List<String> memberIdList = responses.stream()
                                       .map(ImportGroupMemberResponse :: getMemberId)
                                       .collect(Collectors.toList());
        AddGroupMemberPackage addGroupMemberPackage = new AddGroupMemberPackage();
        addGroupMemberPackage.setGroupId(request.getGroupId());
        addGroupMemberPackage.setMembers(memberIdList);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.ADDED_MEMBER, addGroupMemberPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));
        return Result.success(responses);
    }

    @Override
    public Result doAddGroupMember(String groupId, Integer appId, GroupMemberDTO groupMemberDTO) {
        Result<User> singleUserInfo = userService.getSingleUserInfo(groupMemberDTO.getMemberId(), appId);
        if(!singleUserInfo.isSuccess()) {
            return singleUserInfo;
        }
        if(groupMemberDTO.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == groupMemberDTO.getRole()) {
            Integer ownerCount = groupMemberMapper.selectCount(Wrappers.lambdaQuery(GroupMember.class)
                                                                  .eq(GroupMember :: getGroupId, groupId)
                                                                  .eq(GroupMember :: getAppId, appId)
                                                                  .eq(GroupMember :: getRole, GroupMemberRoleEnum.OWNER.getCode()));
            if(ownerCount > 0) {
                return Result.error(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }
        LambdaQueryWrapper<GroupMember> wrapper = Wrappers.lambdaQuery(GroupMember.class)
                                                     .eq(GroupMember :: getGroupId, groupId)
                                                     .eq(GroupMember :: getAppId, appId)
                                                     .eq(GroupMember :: getMemberId, groupMemberDTO.getMemberId());
        GroupMember groupMember = groupMemberMapper.selectOne(wrapper);
        long now = System.currentTimeMillis();
        if(groupMember == null) {
            groupMember = new GroupMember();
            BeanUtils.copyProperties(groupMemberDTO, groupMember);
            groupMember.setGroupId(groupId);
            groupMember.setAppId(appId);
            groupMember.setJoinTime(now);
            int res = groupMemberMapper.insert(groupMember);
            if(res == 1) {
                return Result.success();
            }
        }else if(GroupMemberRoleEnum.LEAVE.getCode() == groupMemberDTO.getRole()){
            groupMember = new GroupMember();
            BeanUtils.copyProperties(groupMemberDTO, groupMember);
            groupMember.setJoinTime(now);
            int update = groupMemberMapper.update(groupMember, wrapper);
            if(update == 1) {
                return Result.success();
            }
        }
        return Result.error(GroupErrorCode.USER_IS_JOINED_GROUP);
    }

    @Override
    public Result<List<GroupMemberVO>> getGroupMember(String groupId, Integer appId) {
        List<GroupMemberVO> groupMemberList = groupMemberMapper.getGroupMember(groupId, appId);
        return Result.success(groupMemberList);
    }

    @Override
    public Result<GetMemberRoleResponse> getMemberRoleInGroup(String groupId, Integer appId, String memberId) {
        GetMemberRoleResponse response = new GetMemberRoleResponse();
        GroupMember groupMember = groupMemberMapper.selectOne(Wrappers.lambdaQuery(GroupMember.class)
                                                                      .eq(GroupMember :: getGroupId, groupId)
                                                                      .eq(GroupMember :: getAppId, appId)
                                                                      .eq(GroupMember :: getMemberId, memberId));
        if(groupMember == null || groupMember.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            return Result.error(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }
        response.setSpeakDate(groupMember.getSpeakDate());
        response.setGroupMemberId(groupMember.getGroupMemberId());
        response.setMemberId(response.getMemberId());
        response.setRole(groupMember.getRole());
        return Result.success(response);
    }

    @Override
    public Result<Collection<String>> getMemberJoinedGroup(GetJoinedGroupRequest request) {
        if(request.getPageParam() != null && request.getPageParam().getPageNum() != null
                && request.getPageParam().getPageSize() != null) {
            PageParam pageParam = request.getPageParam();
            Integer pageNum = pageParam.getPageNum();
            Integer pageSize = pageParam.getPageSize();
            PageUtils.pageCheck(pageNum, pageSize);
            PageHelper.startPage(pageNum, pageSize);
            List<GroupMember> groupMembers = groupMemberMapper.selectList(Wrappers.lambdaQuery(GroupMember.class)
                                                                                  .eq(GroupMember :: getAppId, request.getAppId())
                                                                                  .eq(GroupMember :: getMemberId, request.getMemberId()));
            Set<String> groupIds = new HashSet<>();
            if(CollectionUtil.isNotEmpty(groupMembers)) {
                groupIds = groupMembers.stream().map(GroupMember::getGroupId).collect(Collectors.toSet());
            }
            return Result.success(groupIds);
        }else {
            List<String> joinedGroupIdList = groupMemberMapper.getJoinedGroupIdList(request.getAppId(), request.getMemberId());
            return Result.success(joinedGroupIdList);
        }
    }

    @Override
    public Result transferGroupMember(String ownerId, String groupId, Integer appId) {
        GroupMember oldOwner = new GroupMember();
        oldOwner.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        //更新旧群主
        groupMemberMapper.update(oldOwner, Wrappers.lambdaUpdate(GroupMember.class)
                .eq(GroupMember::getAppId, appId)
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getRole, GroupMemberRoleEnum.OWNER.getCode()));

        //更新新群主
        GroupMember newOwner = new GroupMember();
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupMemberMapper.update(newOwner, Wrappers.lambdaUpdate(GroupMember.class)
                .eq(GroupMember::getAppId, appId)
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberId, ownerId));
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addMember(AddGroupMemberRequest request) {
        List<AddGroupMemberResponse> responses = new ArrayList<>();
        boolean isAdmin = false;
        Result<Group> groupQueryResult = groupService.getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        List<GroupMemberDTO> members = request.getMembers();
        Group group = groupQueryResult.getData();
        /**
         * 私有群（private）	类似普通微信群，创建后仅支持已在群内的好友邀请加群，且无需被邀请方同意或群主审批
         * 公开群（Public）	类似 QQ 群，创建后群主可以指定群管理员，需要群主或管理员审批通过才能入群
         * 群类型 1私有群（类似微信） 2公开群(类似qq）
         */
        if(!isAdmin && GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
        }
        if(configuration.isAddGroupMemberBeforeCallback()) {
            Result result = callbackService.callbackSync(request.getAppId(),
                    Constants.CallbackCommand.GroupMemberAddBefore,
                    JSONObject.toJSONString(request));
            if(!result.isSuccess()) {
                return result;
            }
            try {
                members = JSONArray.parseArray(JSONObject.toJSONString(result.getData()), GroupMemberDTO.class);
            } catch(Exception e) {
                log.error("添加群成员[{}]回调失败, error=[{}]", request, e.getMessage());
            }
        }

        for(GroupMemberDTO member : members) {
            Result result = selfService.doAddGroupMember(request.getGroupId(), request.getAppId(), member);
            AddGroupMemberResponse response = new AddGroupMemberResponse();
            response.setMemberId(member.getMemberId());
            if(result.isSuccess()) {
                response.setResult(0);
            } else if(result.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                response.setResult(2);
                response.setResultMessage(result.getMsg());
            }else {
                response.setResult(1);
                response.setResultMessage(result.getMsg());
            }
            responses.add(response);
        }
        if(configuration.isAddGroupMemberAfterCallback()) {
            AddGroupMemberCallbackRequest callbackRequest = new AddGroupMemberCallbackRequest();
            callbackRequest.setGroupId(request.getGroupId());
            callbackRequest.setOperator(request.getOperator());
            callbackRequest.setGroupType(group.getGroupType());
            callbackRequest.setMembers(members);
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.GroupMemberAddAfter,
                    JSONObject.toJSONString(callbackRequest));
        }

        List<String> memberIdList = responses.stream()
                                        .map(AddGroupMemberResponse :: getMemberId)
                                        .collect(Collectors.toList());
        AddGroupMemberPackage addGroupMemberPackage = new AddGroupMemberPackage();
        addGroupMemberPackage.setGroupId(request.getGroupId());
        addGroupMemberPackage.setMembers(memberIdList);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.ADDED_MEMBER, addGroupMemberPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));
        return Result.success(responses);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result removeMember(RemoveGroupMemberRequest request) {
        boolean isAdmin = false;
        Result<Group> groupQueryResult = groupService.getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        Group group = groupQueryResult.getData();
        if(!isAdmin) {
            //获取操作人的权限
            Result<GetMemberRoleResponse> memberRoleQueryResult = getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getOperator());
            if(!memberRoleQueryResult.isSuccess()) {
                return memberRoleQueryResult;
            }
            GetMemberRoleResponse memberRole = memberRoleQueryResult.getData();
            Integer role = memberRole.getRole();

            boolean isOwner = role == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = role == GroupMemberRoleEnum.MAMAGER.getCode();

            if(!isOwner && isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            //私有群必须要群主才能够踢人
            if(!isOwner && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }

            //公开群管理员和群主都可以踢人
            //管理员只能够踢普通成员
            if(GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
                //获取被踢人的权限
                Result<GetMemberRoleResponse> toBeRemoveMemberRoleQueryResult = getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getMemberId());
                if(!toBeRemoveMemberRoleQueryResult.isSuccess()) {
                    return toBeRemoveMemberRoleQueryResult;
                }
                Integer toBeRemoveMemberRole = toBeRemoveMemberRoleQueryResult.getData().getRole();
                if(toBeRemoveMemberRole == GroupMemberRoleEnum.OWNER.getCode()) {
                    throw new ApplicationException(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                }
                if(isManager && toBeRemoveMemberRole != GroupMemberRoleEnum.ORDINARY.getCode()) {
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }
            }
        }
        Result result = selfService.doRemoveGroupMember(request.getGroupId(), request.getAppId(), request.getMemberId());
        if(result.isSuccess()) {
            if(configuration.isDeleteGroupMemberAfterCallback()) {
                callbackService.callbackAsync(request.getAppId(),
                        Constants.CallbackCommand.GroupMemberDeleteAfter,
                        JSONObject.toJSONString(request));
            }

            RemoveGroupMemberPackage removeGroupMemberPackage = new RemoveGroupMemberPackage();
            removeGroupMemberPackage.setGroupId(request.getGroupId());
            removeGroupMemberPackage.setMember(request.getMemberId());
            groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.DELETED_MEMBER, removeGroupMemberPackage,
                    new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));
        }
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result doRemoveGroupMember(String groupId, Integer appId, String memberId) {
        Result singleUserInfo = userService.getSingleUserInfo(memberId, appId);
        if(!singleUserInfo.isSuccess()) {
            return singleUserInfo;
        }
        Result<GetMemberRoleResponse> memberRole = getMemberRoleInGroup(groupId, appId, memberId);
        if(!memberRole.isSuccess()) {
            return memberRole;
        }
        GetMemberRoleResponse data = memberRole.getData();
        GroupMember removedMember = new GroupMember();
        removedMember.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        removedMember.setLeaveTime(System.currentTimeMillis());
        removedMember.setGroupMemberId(data.getGroupMemberId());
        groupMemberMapper.updateById(removedMember);
        return Result.success();
    }

    @Override
    public Result updateGroupMember(UpdateGroupMemberRequest request) {
        boolean isAdmin = false;
        Result<Group> groupQueryResult = groupService.getGroup(request.getGroupId(), request.getAppId());
        if(!groupQueryResult.isSuccess()) {
            return groupQueryResult;
        }
        Group group = groupQueryResult.getData();
        if(group.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            throw new BadRequestException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        //是否是修改自己的资料
        boolean isOperateMyself = request.getOperator().equals(request.getMemberId());
        if(!isAdmin) {
            //昵称只能够修改自己的
            if(StrUtil.isNotEmpty(request.getAlias()) && !isOperateMyself) {
                throw new BadRequestException(GroupErrorCode.THIS_OPERATE_NEED_ONESELF);
            }

            //修改权限相关信息
            if(request.getRole() != null) {
                //不能直接更新群主信息
                if(request.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                    throw new BadRequestException("无法直接更换群主信息");
                }

                //被操作的人是否在群内
                Result<GetMemberRoleResponse> memberRoleQueryResult = getMemberRoleInGroup(request.getGroupId(), request.getAppId(), request.getMemberId());
                if(!memberRoleQueryResult.isSuccess()) {
                    return memberRoleQueryResult;
                }

                GetMemberRoleResponse data = memberRoleQueryResult.getData();
                Integer role = data.getRole();
                boolean isOwner = role == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = role == GroupMemberRoleEnum.MAMAGER.getCode();
                if(!isOwner && !isManager) {
                    return Result.error(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                //管理员只有群主能够设置
                if(!isOwner) {
                    return Result.error(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }
            }
        }

        GroupMember updateInfo = new GroupMember();
        if(StrUtil.isNotEmpty(request.getAlias())) {
            updateInfo.setAlias(request.getAlias());
        }
        if(request.getRole() != null && request.getRole() == GroupMemberRoleEnum.MAMAGER.getCode()) {
            updateInfo.setRole(GroupMemberRoleEnum.MAMAGER.getCode());
        }
        groupMemberMapper.update(updateInfo, Wrappers.lambdaUpdate(GroupMember.class)
                .eq(GroupMember::getAppId, request.getAppId())
                .eq(GroupMember::getMemberId, request.getMemberId())
                .eq(GroupMember::getGroupId, request.getGroupId()));

        UpdateGroupMemberPackage updateGroupMemberPackage = new UpdateGroupMemberPackage();
        BeanUtils.copyProperties(request, updateGroupMemberPackage);
        groupMessageProducer.sendMessage(request.getOperator(), GroupEventCommand.UPDATED_GROUP, updateGroupMemberPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));

        return Result.success();
    }

    @Override
    public List<String> getGroupMemberId(String groupId, Integer appId) {
        return groupMemberMapper.getGroupMemberId(groupId, appId);
    }

    @Override
    public List<GroupMemberVO> getGroupManager(String groupId, Integer appId) {
        List<GroupMember> managers = groupMemberMapper.selectList(Wrappers.lambdaQuery(GroupMember.class)
                                                                              .eq(GroupMember :: getAppId, appId)
                                                                              .eq(GroupMember :: getGroupId, groupId)
                                                                              .in(GroupMember :: getRole, Arrays.asList(GroupMemberRoleEnum.MAMAGER.getCode(), GroupMemberRoleEnum.OWNER.getCode())));
        List<GroupMemberVO> result = managers.stream().map(manager -> {
            GroupMemberVO groupMemberVO = new GroupMemberVO();
            BeanUtils.copyProperties(manager, groupMemberVO);
            return groupMemberVO;
        }).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<String> getJoinedGroupIdList(Integer appId, String userId) {
        return groupMemberMapper.getJoinedGroupIdList(appId, userId);
    }
}
