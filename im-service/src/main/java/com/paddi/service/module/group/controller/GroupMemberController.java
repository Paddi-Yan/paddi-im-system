package com.paddi.service.module.group.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.group.model.req.AddGroupMemberRequest;
import com.paddi.service.module.group.model.req.ImportGroupMemberRequest;
import com.paddi.service.module.group.model.req.RemoveGroupMemberRequest;
import com.paddi.service.module.group.model.req.UpdateGroupMemberRequest;
import com.paddi.service.module.group.service.GroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:06:06
 */
@RestController
@RequestMapping("/v1/group/member")
public class GroupMemberController {

    @Autowired
    private GroupMemberService groupMemberService;


    @PostMapping("/importGroupMember")
    public Result importGroupMember(@RequestBody @Validated ImportGroupMemberRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupMemberService.importGroupMember(request);
    }

    @RequestMapping("/add")
    public Result addMember(@RequestBody @Validated AddGroupMemberRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupMemberService.addMember(request);
    }

    @RequestMapping("/remove")
    public Result removeMember(@RequestBody @Validated RemoveGroupMemberRequest request, Integer appId, String identifier)  {
        request.setAppId(appId);
        request.setOperator(identifier);
        return groupMemberService.removeMember(request);
    }

    @PutMapping("/update")
    public Result updateGroupMember(@RequestBody @Validated UpdateGroupMemberRequest req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.updateGroupMember(req);
    }
}
