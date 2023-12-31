package com.paddi.service.module.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.user.UserModifyPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.DelFlagEnum;
import com.paddi.common.enums.UserErrorCode;
import com.paddi.common.enums.command.UserEventCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.Result;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.group.service.GroupService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.mapper.UserMapper;
import com.paddi.service.module.user.model.req.*;
import com.paddi.service.module.user.model.resp.GetSyncProgressResponse;
import com.paddi.service.module.user.model.resp.GetUserInfoResponse;
import com.paddi.service.module.user.model.resp.ImportUserResponse;
import com.paddi.service.module.user.service.UserService;
import com.paddi.service.utils.CallbackService;
import com.paddi.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:23:51
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private CallbackService callbackService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GroupService groupService;

    @Override
    public Result importUser(ImportUserRequest importUserRequest) {
        if(importUserRequest.getUserList().size() > 100) {
            return Result.error(UserErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportUserResponse response = new ImportUserResponse();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();
        importUserRequest.getUserList().forEach(user -> {
            try {
                user.setAppId(importUserRequest.getAppId());
                int insert = userMapper.insert(user);
                if(insert == 1) {
                    successId.add(user.getUserId());
                }
            } catch(Exception e) {
                log.error(e.getMessage());
                errorId.add(user.getUserId());
            }
        });
        response.setSuccessId(successId);
        response.setErrorId(errorId);
        return Result.success(response);
    }

    @Override
    public Result deleteUser(DeleteUserRequest request) {
        User user = new User();
        user.setDelFlag(DelFlagEnum.DELETE.getCode());
        List<String> errorId = new ArrayList();
        List<String> successId = new ArrayList();
        for (String userId: request.getUserId()) {
            int update = 0;
            try {
                update = userMapper.update(user, Wrappers.lambdaUpdate(User.class)
                                                              .eq(User :: getAppId, request.getAppId())
                                                              .eq(User :: getUserId, userId)
                                                              .eq(User :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
                if(update > 0){
                    successId.add(userId);
                }else{
                    errorId.add(userId);
                }
            }catch (Exception e){
                errorId.add(userId);
            }
        }
        ImportUserResponse response = new ImportUserResponse();
        response.setSuccessId(successId);
        response.setErrorId(errorId);
        return Result.success(response);
    }

    @Override
    public Result getUserInfo(GetUserInfoRequest request) {
        List<User> userInfoList = userMapper.selectList(Wrappers.lambdaQuery(User.class)
                                                         .eq(User :: getAppId, request.getAppId())
                                                         .in(User :: getUserId, request.getUserIds())
                                                         .eq(User :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
        Map<String, User> userIdToInfoMap = userInfoList.stream()
                                                .collect(Collectors.toMap(User :: getUserId, Function.identity()));
        List<String> failUserList = new ArrayList<>();
        for(String userId : request.getUserIds()) {
            if(!userIdToInfoMap.containsKey(userId)) {
                failUserList.add(userId);
            }
        }
        GetUserInfoResponse getUserInfoResponse = new GetUserInfoResponse(userInfoList, failUserList);
        return Result.success(getUserInfoResponse);
    }

    @Override
    public Result getSingleUserInfo(String userId, Integer appId) {
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class)
                                                 .eq(User :: getAppId, appId)
                                                 .eq(User :: getUserId, userId)
                                                 .eq(User :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
        if(user == null) {
            return Result.error(UserErrorCode.USER_IS_NOT_EXIST);
        }
        return Result.success(user);
    }

    @Override
    public Result modifyUserInfo(ModifyUserInfoRequest request) {
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class)
                                                 .eq(User :: getAppId, request.getAppId())
                                                 .eq(User :: getUserId, request.getUserId())
                                                 .eq(User :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
        if(user == null) {
            return Result.error(UserErrorCode.USER_IS_NOT_EXIST);
        }
        User updateUserInfo = new User();
        BeanUtils.copyProperties(user, updateUserInfo);
        updateUserInfo.setAppId(null);
        updateUserInfo.setUserId(null);
        int update = userMapper.update(updateUserInfo, Wrappers.lambdaUpdate(User.class)
                                                               .eq(User :: getAppId, request.getAppId())
                                                               .eq(User :: getUserId, request.getUserId())
                                                               .eq(User :: getDelFlag, DelFlagEnum.NORMAL.getCode()));
        if(update == 1) {

            UserModifyPackage userModifyPackage = new UserModifyPackage();
            BeanUtils.copyProperties(request, userModifyPackage);
            messageProducer.sendToUser(request.getUserId(), request.getAppId(),
                    request.getClientType(), request.getImei(),
                    UserEventCommand.USER_MODIFY, userModifyPackage);

            if(configuration.isModifyUserAfterCallback()) {
                callbackService.callbackAsync(request.getAppId(),
                        Constants.CallbackCommand.ModifyUserAfter,
                        JSONObject.toJSONString(request));
            }
            return Result.success();
        }
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    @Override
    public Result getSyncProgress(GetSyncProgressRequest request) {
        String hashKey = request.getAppId() + Constants.RedisConstants.SEQUENCE_PREFIX + request.getUserId();
        Map<Object, Object> syncProgressSequenceMap = redisTemplate.opsForHash().entries(hashKey);
        Long groupMaxSequence = groupService.getGroupMaxSequence(request.getAppId(), request.getUserId());
        syncProgressSequenceMap.put(Constants.SequenceConstants.Group, String.valueOf(groupMaxSequence));
        GetSyncProgressResponse getSyncProgressResponse = new GetSyncProgressResponse(syncProgressSequenceMap);
        return Result.success(getSyncProgressResponse);
    }
}