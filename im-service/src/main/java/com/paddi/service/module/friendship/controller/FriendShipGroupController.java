package com.paddi.service.module.friendship.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.model.req.AddFriendShipGroupRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupMemberRequest;
import com.paddi.service.module.friendship.model.req.DeleteFriendShipGroupRequest;
import com.paddi.service.module.friendship.service.FriendShipGroupMemberService;
import com.paddi.service.module.friendship.service.FriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:34:40
 */
@RestController
@RequestMapping("/v1/friendship/group")
public class FriendShipGroupController {

    @Autowired
    private FriendShipGroupService friendShipGroupService;

    @Autowired
    private FriendShipGroupMemberService friendShipGroupMemberService;

    @PostMapping
    public Result addGroup(@RequestBody @Validated AddFriendShipGroupRequest request, Integer appId) {
        request.setAppId(appId);
        return friendShipGroupService.addGroup(request);
    }

    @DeleteMapping
    public Result deleteGroup(@RequestBody @Validated DeleteFriendShipGroupRequest request, Integer appId)  {
        request.setAppId(appId);
        return friendShipGroupService.deleteGroup(request);
    }


    @RequestMapping("/member/add")
    public Result addGroupMember(@RequestBody @Validated AddFriendShipGroupMemberRequest request, Integer appId)  {
        request.setAppId(appId);
        return friendShipGroupMemberService.addGroupNumber(request);
    }

    @RequestMapping("/member/del")
    public Result deleteGroupMember(@RequestBody @Validated DeleteFriendShipGroupMemberRequest request, Integer appId)  {
        request.setAppId(appId);
        return friendShipGroupMemberService.deleteGroupMember(request);
    }

}
