package com.paddi.service.module.group.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.group.model.req.*;
import com.paddi.service.module.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:05:54
 */
@RestController
@RequestMapping("v1/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/importGroup")
    public Result importGroup(@RequestBody @Validated ImportGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.importGroup(request);
    }

    @PostMapping("/createGroup")
    public Result createGroup(@RequestBody @Validated CreateGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.createGroup(request);
    }

    @GetMapping("/getGroupInfo")
    public Result getGroupInfo(@RequestBody @Validated GetGroupRequest request, Integer appId)  {
        request.setAppId(appId);
        return groupService.getGroup(request);
    }

    @RequestMapping("/updateGroupInfo")
    public Result update(@RequestBody @Validated UpdateGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.updateBaseGroupInfo(request);
    }

    @RequestMapping("/getJoinedGroup")
    public Result getJoinedGroup(@RequestBody @Validated GetJoinedGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.getJoinedGroup(request);
    }

    @PostMapping("/destroyGroup")
    public Result destroyGroup(@RequestBody @Validated DestroyGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.destroyGroup(request);
    }

    @PostMapping("/transferGroup")
    public Result transferGroup(@RequestBody @Validated TransferGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.transferGroup(request);
    }

    @RequestMapping("/forbidSendMessage")
    public Result forbidSendMessage(@RequestBody @Validated MuteGroupRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupService.muteGroup(request);
    }

}
