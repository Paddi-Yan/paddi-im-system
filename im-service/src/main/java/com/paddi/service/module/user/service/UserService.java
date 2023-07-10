package com.paddi.service.module.user.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.model.req.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:23:24
 */
public interface UserService {

    Result importUser(ImportUserRequest importUserRequest);

    Result deleteUser(DeleteUserRequest request);

    Result getUserInfo(GetUserInfoRequest request);

    Result<User> getSingleUserInfo(String userId, Integer appId);

    Result modifyUserInfo(ModifyUserInfoRequest request);

    Result getSyncProgress(GetSyncProgressRequest request);
}
