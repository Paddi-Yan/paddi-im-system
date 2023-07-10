package com.paddi.service.module.user.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.user.model.req.*;
import com.paddi.service.module.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:36:06
 */
@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/importUser")
    public Result importUser(@RequestBody ImportUserRequest request, Integer appId) {
        request.setAppId(appId);
        return userService.importUser(request);
    }

    @DeleteMapping("/deleteUser")
    public Result deleteUser(@RequestBody @Validated DeleteUserRequest request, Integer appId) {
        request.setAppId(appId);
        return userService.deleteUser(request);
    }

    @RequestMapping("/getUserInfo")
    public Result getUserInfo(@RequestBody GetUserInfoRequest request, Integer appId){//@Validated
        request.setAppId(appId);
        return userService.getUserInfo(request);
    }

    @RequestMapping("/getSingleUserInfo")
    public Result getSingleUserInfo(@RequestBody @Validated GetSingleUserInfoRequest request, Integer appId){
        return userService.getSingleUserInfo(request.getUserId(), appId);
    }

    @RequestMapping("/modifyUserInfo")
    public Result modifyUserInfo(@RequestBody @Validated ModifyUserInfoRequest request, Integer appId){
        request.setAppId(appId);
        return userService.modifyUserInfo(request);
    }

    @GetMapping("/getSyncProgress")
    public Result getSyncProgress(@RequestBody GetSyncProgressRequest request) {
        return userService.getSyncProgress(request);
    }


}

