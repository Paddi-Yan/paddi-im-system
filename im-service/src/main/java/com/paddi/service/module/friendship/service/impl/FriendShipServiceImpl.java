package com.paddi.service.module.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.common.enums.AllowFriendTypeEnum;
import com.paddi.common.enums.CheckFriendShipTypeEnum;
import com.paddi.common.enums.FriendShipErrorCode;
import com.paddi.common.enums.FriendShipStatusEnum;
import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import com.paddi.service.module.friendship.entity.po.FriendShip;
import com.paddi.service.module.friendship.mapper.FriendShipMapper;
import com.paddi.service.module.friendship.model.req.*;
import com.paddi.service.module.friendship.model.resp.CheckFriendShipResponse;
import com.paddi.service.module.friendship.model.resp.ImportFriendShipResponse;
import com.paddi.service.module.friendship.service.FriendShipRequestService;
import com.paddi.service.module.friendship.service.FriendShipService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:34:39
 */
@Service
@Slf4j
public class FriendShipServiceImpl implements FriendShipService {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendShipMapper friendShipMapper;

    @Autowired
    private FriendShipRequestService friendShipRequestService;


    @Override
    public Result importFriendShip(ImportFriendShipRequest request) {

        if(request.getFriendItem().size() > 100) {
            return Result.error(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResponse response = new ImportFriendShipResponse();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();
        for(ImportFriendShipRequest.ImportFriendDTO importFriendDTO : request.getFriendItem()) {
            FriendShip friendShip = new FriendShip();
            BeanUtils.copyProperties(importFriendDTO, friendShip);
            friendShip.setAppId(request.getAppId());
            friendShip.setFromId(request.getFromId());
            try {
                int insert = friendShipMapper.insert(friendShip);
                if(insert == 1) {
                    successId.add(importFriendDTO.getToId());
                }else {
                    errorId.add(importFriendDTO.getToId());
                }
            } catch(Exception e) {
                log.error(e.getMessage());
                errorId.add(importFriendDTO.getToId());
            }
        }
        response.setSuccessId(successId);
        response.setErrorId(errorId);
        return Result.success(response);
    }

    @Override
    public Result addFriend(AddFriendRequest request) {
        Result<User> fromInfo = userService.getSingleUserInfo(request.getFromId(), request.getAppId());
        if(!fromInfo.isSuccess()) {
            return fromInfo;
        }
        Result<User> toInfo = userService.getSingleUserInfo(request.getToItem().getToId(), request.getAppId());
        if(!toInfo.isSuccess()) {
            return toInfo;
        }
        User toInfoData = toInfo.getData();
        if(toInfoData.getFriendAllowType() != null && toInfoData.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            //无需进行好友添加验证
            doAddFriend(request.getFromId(), request.getToItem(), request.getAppId());
        }else {
            LambdaQueryWrapper<FriendShip> wrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                        .eq(FriendShip :: getAppId, request.getAppId())
                                                        .eq(FriendShip :: getFromId, request.getFromId())
                                                        .eq(FriendShip :: getToId, request.getToItem());
            FriendShip friendShip = friendShipMapper.selectOne(wrapper);
            if(friendShip == null || friendShip.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                Result result = friendShipRequestService.addFriendShipRequest(request.getFromId(), request.getToItem(), request.getAppId());
                if(!result.isSuccess()) {
                    return result;
                }
            }else {
                return Result.error(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }
        return Result.success();
    }

    @Transactional
    @Override
    public Result doAddFriend(String fromId, FriendDTO friendDTO, Integer appId) {
        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在 如果存在则判断状态 如果已添加提示已添加 如果未添加则修改状态
        FriendShip fromItem = friendShipMapper.selectOne(Wrappers.lambdaQuery(FriendShip.class)
                                                                   .eq(FriendShip :: getAppId, appId)
                                                                   .eq(FriendShip :: getFromId, fromId)
                                                                   .eq(FriendShip :: getToId, friendDTO.getToId()));
        if(fromItem == null) {
            //添加好友
            fromItem = new FriendShip();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            BeanUtils.copyProperties(friendDTO, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = friendShipMapper.insert(fromItem);
            if(insert != 1) {
                return Result.error(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        }else {
            //判断状态
            if(fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return Result.success(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }else {
                FriendShip updateFriendShip = new FriendShip();
                if(StringUtils.isNotBlank(friendDTO.getAddSource())){
                    updateFriendShip.setAddSource(friendDTO.getAddSource());
                }

                if(StringUtils.isNotBlank(friendDTO.getRemark())){
                    updateFriendShip.setRemark(friendDTO.getRemark());
                }

                if(StringUtils.isNotBlank(friendDTO.getExtra())){
                    updateFriendShip.setExtra(friendDTO.getExtra());
                }

                updateFriendShip.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int update = friendShipMapper.update(updateFriendShip, Wrappers.lambdaUpdate(FriendShip.class)
                                                                               .eq(FriendShip :: getAppId, appId)
                                                                               .eq(FriendShip :: getFromId, fromId)
                                                                               .eq(FriendShip :: getToId, friendDTO.getToId()));
                if(update != 1) {
                    return Result.error(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        FriendShip toFriendShip = friendShipMapper.selectOne(Wrappers.lambdaQuery(FriendShip.class)
                                                                   .eq(FriendShip :: getAppId, appId)
                                                                   .eq(FriendShip :: getFromId, friendDTO.getToId())
                                                                   .eq(FriendShip :: getToId, fromId));
        if(toFriendShip == null) {
            toFriendShip = new FriendShip();
            toFriendShip.setAppId(appId);
            toFriendShip.setFromId(friendDTO.getToId());
            BeanUtils.copyProperties(friendDTO,toFriendShip);
            toFriendShip.setToId(fromId);
            toFriendShip.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toFriendShip.setCreateTime(System.currentTimeMillis());
            friendShipMapper.insert(toFriendShip);
        }else {
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toFriendShip.getStatus()){
                FriendShip updateFriendShip = new FriendShip();
                updateFriendShip.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                friendShipMapper.update(updateFriendShip,Wrappers.lambdaUpdate(FriendShip.class)
                                                                 .eq(FriendShip :: getAppId, appId)
                                                                 .eq(FriendShip :: getFromId, friendDTO.getToId())
                                                                 .eq(FriendShip :: getToId, fromId));
            }
        }
        return Result.success();
    }

    @Override
    public Result deleteFriend(DeleteFriendRequest request) {
        LambdaQueryWrapper<FriendShip> queryWrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                    .eq(FriendShip :: getAppId, request.getAppId())
                                                    .eq(FriendShip :: getFromId, request.getFromId())
                                                    .eq(FriendShip :: getToId, request.getToId());
        FriendShip friendShip = friendShipMapper.selectOne(queryWrapper);
        if(friendShip == null) {
            return Result.error(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }
        if(friendShip.getStatus() != null && friendShip.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
            FriendShip updateInfo = new FriendShip();
            updateInfo.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
            friendShipMapper.update(updateInfo, queryWrapper);
        }
        return Result.success();
    }

    @Override
    public Result deleteAllFriend(DeleteFriendRequest request) {
        LambdaQueryWrapper<FriendShip> queryWrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                    .eq(FriendShip :: getAppId, request.getAppId())
                                                    .eq(FriendShip :: getFriendSequence, request.getFromId())
                                                    .eq(FriendShip :: getStatus, FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode());
        FriendShip updateInfo = new FriendShip();
        updateInfo.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        friendShipMapper.update(updateInfo, queryWrapper);
        return Result.success();
    }

    @Override
    public Result getAllFriendShip(GetAllFriendShipRequest request) {
        return Result.success(friendShipMapper.selectList(Wrappers.lambdaQuery(FriendShip.class)
                .eq(FriendShip::getAppId, request.getAppId())
                .eq(FriendShip::getFromId, request.getFromId())));
    }

    @Override
    public Result getRelation(GetRelationRequest request) {
        FriendShip friendShip = friendShipMapper.selectOne(Wrappers.lambdaQuery(FriendShip.class)
                                                                   .eq(FriendShip :: getAppId, request.getAppId())
                                                                   .eq(FriendShip :: getFromId, request.getFromId())
                                                                   .eq(FriendShip :: getToId, request.getToId()));
        if(friendShip == null) {
            return Result.error(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return Result.success(friendShip);
    }

    @Override
    public Result checkFriendship(CheckFriendShipRequest request) {
        Map<String, Integer> map = request.getToIds()
                                              .stream()
                                              .collect(Collectors.toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResponse> responses;
        if(request.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            responses = friendShipMapper.checkFriendShip(request);
        }else {
            responses = friendShipMapper.checkFriendShipBoth(request);
        }
        Set<String> toIdSet = responses.stream().map(CheckFriendShipResponse :: getToId).collect(Collectors.toSet());
        for(Map.Entry<String, Integer> entry : map.entrySet()) {
            if(!toIdSet.contains(entry.getKey())) {
                CheckFriendShipResponse response = new CheckFriendShipResponse();
                response.setFromId(request.getFromId());
                response.setToId(entry.getKey());
                response.setStatus(entry.getValue());
                responses.add(response);
            }
        }
        return Result.success(responses);
    }

    @Override
    public Result addBlack(AddFriendShipBlackRequest request) {
        Result<User> fromInfo = userService.getSingleUserInfo(request.getFromId(), request.getAppId());
        if(!fromInfo.isSuccess()) {
            return fromInfo;
        }
        Result<User> toInfo = userService.getSingleUserInfo(request.getToId(), request.getAppId());
        if(!toInfo.isSuccess()) {
            return toInfo;
        }

        LambdaQueryWrapper<FriendShip> wrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                    .eq(FriendShip :: getAppId, request.getAppId())
                                                    .eq(FriendShip :: getFromId, request.getFromId())
                                                    .eq(FriendShip :: getToId, request.getToId());
        FriendShip friendShip = friendShipMapper.selectOne(wrapper);
        if(friendShip == null) {
            FriendShip blackFriendShip = FriendShip.builder()
                                         .fromId(request.getFromId())
                                         .toId(request.getToId())
                                         .appId(request.getAppId())
                                         .black(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode())
                                         .createTime(System.currentTimeMillis())
                                         .build();
            friendShipMapper.insert(blackFriendShip);
        }else {
            if(friendShip.getBlack() != null && friendShip.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return Result.error(FriendShipErrorCode.FRIEND_IS_BLACK);
            }else {
                FriendShip update = new FriendShip();
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = friendShipMapper.update(update, wrapper);
                if(result != 1) {
                    return Result.error(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result deleteBlack(DeleteBlackRequest request) {
        LambdaQueryWrapper<FriendShip> wrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                         .eq(FriendShip :: getAppId, request.getAppId())
                                                         .eq(FriendShip :: getFromId, request.getFromId())
                                                         .eq(FriendShip :: getToId, request.getToId());
        FriendShip friendShip = friendShipMapper.selectOne(wrapper);
        if(friendShip == null || (friendShip.getBlack() != null && friendShip.getBlack() ==FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode())) {
            return Result.error(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }
        FriendShip update = new FriendShip();
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int result = friendShipMapper.update(update, wrapper);
        if(result != 1) {
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result checkBlack(CheckFriendShipRequest request) {
        Map<String, Integer> map = request.getToIds()
                                              .stream()
                                              .collect(Collectors.toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResponse> responses;
        if(request.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            responses = friendShipMapper.checkBlackFriendShip(request);
        }else {
            responses = friendShipMapper.checkBlackFriendShipBoth(request);
        }
        Set<CheckFriendShipResponse> toIdSet = responses.stream().collect(Collectors.toSet());
        for(Map.Entry<String, Integer> entry : map.entrySet()) {
            if(!toIdSet.contains(entry.getKey())) {
                CheckFriendShipResponse response = new CheckFriendShipResponse();
                response.setFromId(request.getFromId());
                response.setToId(entry.getKey());
                response.setStatus(entry.getValue());
                responses.add(response);
            }
        }
        return Result.success(responses);
    }


}
