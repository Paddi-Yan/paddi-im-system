package com.paddi.service.module.friendship.controller;

import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.model.req.*;
import com.paddi.service.module.friendship.service.FriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:33:10
 */
@RestController
@RequestMapping("/v1/friendship")
public class FriendShipController {

    @Autowired
    FriendShipService friendShipService;

    @PostMapping("/importFriendShip")
    public Result importFriendShip(@RequestBody @Validated ImportFriendShipRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.importFriendShip(request);
    }

    @PostMapping("/addFriend")
    public Result addFriend(@RequestBody @Validated AddFriendRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.addFriend(request);
    }

    @DeleteMapping("/deleteFriend")
    public Result deleteFriend(@RequestBody @Validated DeleteFriendRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.deleteFriend(request);
    }

    @DeleteMapping("/deleteAllFriend")
    public Result deleteAllFriend(@RequestBody @Validated DeleteFriendRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.deleteAllFriend(request);
    }

    @GetMapping("/getAllFriendShip")
    public Result getAllFriendShip(@RequestBody @Validated GetAllFriendShipRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.getAllFriendShip(request);
    }

    @RequestMapping("/getRelation")
    public Result getRelation(@RequestBody @Validated GetRelationRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.getRelation(request);
    }

    @RequestMapping("/checkFriend")
    public Result checkFriend(@RequestBody @Validated CheckFriendShipRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.checkFriendship(request);
    }
    @RequestMapping("/addBlack")
    public Result addBlack(@RequestBody @Validated AddFriendShipBlackRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.addBlack(request);
    }

    @RequestMapping("/deleteBlack")
    public Result deleteBlack(@RequestBody @Validated DeleteBlackRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.deleteBlack(request);
    }

    @RequestMapping("/checkBlack")
    public Result checkBlck(@RequestBody @Validated CheckFriendShipRequest request, Integer appId){
        request.setAppId(appId);
        return friendShipService.checkBlack(request);
    }
}
