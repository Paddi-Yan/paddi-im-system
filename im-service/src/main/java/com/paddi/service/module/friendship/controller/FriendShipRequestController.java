package com.paddi.service.module.friendship.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.model.req.ApproveFriendRequestReq;
import com.paddi.service.module.friendship.model.req.GetFriendShipRequestReq;
import com.paddi.service.module.friendship.model.req.ReadFriendShipRequestReq;
import com.paddi.service.module.friendship.service.FriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 16:17:29
 */
@RestController
@RequestMapping("/v1/friendship/request")
public class FriendShipRequestController {

    @Autowired
    private FriendShipRequestService friendShipRequestService;

    @RequestMapping("/approveFriendRequest")
    public Result approveFriendRequest(@RequestBody @Validated ApproveFriendRequestReq request, Integer appId, String identifier){
        request.setAppId(appId);
        request.setOperator(identifier);
        return friendShipRequestService.approveFriendRequest(request);
    }
    @RequestMapping("/getFriendRequest")
    public Result getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq request, Integer appId){
        request.setAppId(appId);
        return friendShipRequestService.getFriendRequest(request.getFromId(),request.getAppId());
    }

    @RequestMapping("/readFriendShipRequestReq")
    public Result readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return friendShipRequestService.readFriendShipRequestReq(req);
    }
}
