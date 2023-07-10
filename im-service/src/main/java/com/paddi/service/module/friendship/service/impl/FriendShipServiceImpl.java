package com.paddi.service.module.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.friend.*;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.AllowFriendTypeEnum;
import com.paddi.common.enums.CheckFriendShipTypeEnum;
import com.paddi.common.enums.FriendShipErrorCode;
import com.paddi.common.enums.FriendShipStatusEnum;
import com.paddi.common.enums.command.FriendshipEventCommand;
import com.paddi.common.model.BaseRequest;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.common.model.message.SyncResponse;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import com.paddi.service.module.friendship.entity.po.FriendShip;
import com.paddi.service.module.friendship.mapper.FriendShipMapper;
import com.paddi.service.module.friendship.model.callback.AddFriendAfterCallbackRequest;
import com.paddi.service.module.friendship.model.callback.AddFriendBlackAfterCallbackRequest;
import com.paddi.service.module.friendship.model.callback.DeleteFriendAfterCallbackRequest;
import com.paddi.service.module.friendship.model.callback.UpdateFriendAfterCallbackRequest;
import com.paddi.service.module.friendship.model.req.*;
import com.paddi.service.module.friendship.model.resp.CheckFriendShipResponse;
import com.paddi.service.module.friendship.model.resp.ImportFriendShipResponse;
import com.paddi.service.module.friendship.service.FriendShipRequestService;
import com.paddi.service.module.friendship.service.FriendShipService;
import com.paddi.service.module.user.entity.po.User;
import com.paddi.service.module.user.service.UserService;
import com.paddi.service.utils.CallbackService;
import com.paddi.service.utils.DataSequenceUtils;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
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

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private CallbackService callbackService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;

    @Autowired
    private DataSequenceUtils sequenceUtils;

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

        if(configuration.isAddFriendBeforeCallback()) {
            Result result = callbackService.callbackSync(request.getAppId(),
                    Constants.CallbackCommand.AddFriendBefore,
                    JSONObject.toJSONString(request));
            if(!result.isSuccess()) {
                return result;
            }
        }

        User toInfoData = toInfo.getData();
        if(toInfoData.getFriendAllowType() != null && toInfoData.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            //无需进行好友添加验证
            return doAddFriend(request, request.getFromId(), request.getToItem(), request.getAppId());
        }else {
            LambdaQueryWrapper<FriendShip> wrapper = Wrappers.lambdaQuery(FriendShip.class)
                                                        .eq(FriendShip :: getAppId, request.getAppId())
                                                        .eq(FriendShip :: getFromId, request.getFromId())
                                                        .eq(FriendShip :: getToId, request.getToItem().getToId());
            FriendShip friendShip = friendShipMapper.selectOne(wrapper);
            if(friendShip == null || friendShip.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                Result result = friendShipRequestService.addFriendShipRequest(request, request.getFromId(), request.getToItem(), request.getAppId());
                if(!result.isSuccess()) {
                    return result;
                }
            }else {
                return Result.error(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }
        return Result.success();
    }

    @Override
    public Result updateFriend(UpdateFriendRequest request) {
        Result<User> fromInfoQueryResult = userService.getSingleUserInfo(request.getFromId(), request.getAppId());
        if(!fromInfoQueryResult.isSuccess()) {
            return fromInfoQueryResult;
        }
        Result<User> toInfoQueryResult = userService.getSingleUserInfo(request.getToItem().getToId(), request.getAppId());
        if(!toInfoQueryResult.isSuccess()) {
            return toInfoQueryResult;
        }

        if(configuration.isModifyFriendBeforeCallback()) {
            UpdateFriendAfterCallbackRequest callbackRequest = new UpdateFriendAfterCallbackRequest();
            callbackRequest.setFromId(request.getFromId());
            callbackRequest.setToItem(request.getToItem());
            Result result = callbackService.callbackSync(request.getAppId(),
                    Constants.CallbackCommand.UpdateFriendBefore,
                    JSONObject.toJSONString(callbackRequest));
            if(!result.isSuccess()) {
                return result;
            }
        }

        Result result = doUpdate(request.getFromId(), request.getToItem(), request.getAppId());
        if(result.isSuccess()) {
            UpdateFriendPackage updateFriendPackage = new UpdateFriendPackage();
            updateFriendPackage.setRemark(request.getToItem().getRemark());
            updateFriendPackage.setToId(request.getToItem().getToId());
            messageProducer.sendToUser(request.getFromId(), request.getAppId(),
                    request.getClientType(), request.getImei(), FriendshipEventCommand.FRIEND_UPDATE, updateFriendPackage);

            if(configuration.isModifyFriendAfterCallback()) {
                UpdateFriendAfterCallbackRequest callbackRequest = new UpdateFriendAfterCallbackRequest();
                callbackRequest.setFromId(request.getFromId());
                callbackRequest.setToItem(request.getToItem());
                callbackService.callbackAsync(request.getAppId(),
                        Constants.CallbackCommand.UpdateFriendAfter,
                        JSONObject.toJSONString(callbackRequest));
            }
        }

        return result;
    }

    private Result doUpdate(String fromId, FriendDTO toItem, Integer appId) {
        int update = friendShipMapper.update(null, Wrappers.lambdaUpdate(FriendShip.class)
                                                           .set(FriendShip :: getAddSource, toItem.getAddSource())
                                                           .set(FriendShip :: getExtra, toItem.getExtra())
                                                           .set(FriendShip :: getRemark, toItem.getRemark())
                                                           .eq(FriendShip :: getAppId, appId)
                                                           .eq(FriendShip :: getToId, toItem.getToId())
                                                           .eq(FriendShip :: getFromId, fromId));
        if(update == 1) {
            return Result.success();
        }
        return Result.error();
    }

    @Transactional
    @Override
    public Result doAddFriend(BaseRequest baseRequest, String fromId, FriendDTO friendDTO, Integer appId) {
        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在 如果存在则判断状态 如果已添加提示已添加 如果未添加则修改状态
        FriendShip fromItem = friendShipMapper.selectOne(Wrappers.lambdaQuery(FriendShip.class)
                                                                   .eq(FriendShip :: getAppId, appId)
                                                                   .eq(FriendShip :: getFromId, fromId)
                                                                   .eq(FriendShip :: getToId, friendDTO.getToId()));
        Long sequence = 0L;
        if(fromItem == null) {
            //添加好友
            fromItem = new FriendShip();
            sequence = sequenceGenerator.generate(appId + ":" + Constants.SequenceConstants.Friendship);
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            fromItem.setFriendSequence(sequence);
            BeanUtils.copyProperties(friendDTO, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = friendShipMapper.insert(fromItem);
            if(insert != 1) {
                return Result.error(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            sequenceUtils.writeSequence(appId, fromId, Constants.SequenceConstants.Friendship, sequence);
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
                sequence = sequenceGenerator.generate(appId + ":" +  Constants.SequenceConstants.Friendship);
                updateFriendShip.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                updateFriendShip.setFriendSequence(sequence);

                int update = friendShipMapper.update(updateFriendShip, Wrappers.lambdaUpdate(FriendShip.class)
                                                                               .eq(FriendShip :: getAppId, appId)
                                                                               .eq(FriendShip :: getFromId, fromId)
                                                                               .eq(FriendShip :: getToId, friendDTO.getToId()));
                if(update != 1) {
                    return Result.error(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
                sequenceUtils.writeSequence(appId, fromId, Constants.SequenceConstants.Friendship, sequence);
            }
        }

        FriendShip toItem = friendShipMapper.selectOne(Wrappers.lambdaQuery(FriendShip.class)
                                                                   .eq(FriendShip :: getAppId, appId)
                                                                   .eq(FriendShip :: getFromId, friendDTO.getToId())
                                                                   .eq(FriendShip :: getToId, fromId));
        if(toItem == null) {
            toItem = new FriendShip();
            toItem.setAppId(appId);
            toItem.setFromId(friendDTO.getToId());
            BeanUtils.copyProperties(friendDTO,toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setFriendSequence(sequence);
            friendShipMapper.insert(toItem);
            sequenceUtils.writeSequence(appId, friendDTO.getToId(), Constants.SequenceConstants.Friendship, sequence);
        }else {
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toItem.getStatus()){
                FriendShip updateFriendShip = new FriendShip();
                updateFriendShip.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                updateFriendShip.setFriendSequence(sequence);
                int update = friendShipMapper.update(updateFriendShip, Wrappers.lambdaUpdate(FriendShip.class)
                                                                               .eq(FriendShip :: getAppId, appId)
                                                                               .eq(FriendShip :: getFromId, friendDTO.getToId())
                                                                               .eq(FriendShip :: getToId, fromId));
                if(update != 1) {
                    return Result.error(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
                sequenceUtils.writeSequence(appId, friendDTO.getToId(), Constants.SequenceConstants.Friendship, sequence);
            }
        }

        //向双方发送消息
        AddFriendPackage addFriendPackage = new AddFriendPackage();
        BeanUtils.copyProperties(fromItem, addFriendPackage);
        addFriendPackage.setSequence(sequence);
        if(baseRequest != null) {
            messageProducer.sendToUser(fromId, baseRequest.getAppId(), baseRequest.getClientType(),
                    baseRequest.getImei(), FriendshipEventCommand.FRIEND_ADD, addFriendPackage);
        }else {
            messageProducer.sendToAllUserTerminal(fromId, FriendshipEventCommand.FRIEND_ADD,
                    addFriendPackage, appId);
        }

        messageProducer.sendToAllUserTerminal(toItem.getFromId(), FriendshipEventCommand.FRIEND_ADD, addFriendPackage, appId);

        //添加完好友之后的回调
        if(configuration.isAddFriendAfterCallback()) {
            AddFriendAfterCallbackRequest callbackRequest = new AddFriendAfterCallbackRequest();
            callbackRequest.setFromId(fromId);
            callbackRequest.setToItem(friendDTO);
            callbackService.callbackAsync(appId,
                    Constants.CallbackCommand.AddFriendAfter,
                    JSONObject.toJSONString(callbackRequest));
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
            Long sequence = sequenceGenerator.generate(request.getAppId() + ":" +  Constants.SequenceConstants.Friendship);
            updateInfo.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
            updateInfo.setFriendSequence(sequence);
            int update = friendShipMapper.update(updateInfo, queryWrapper);
            if(update == 1) {
                sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Friendship, sequence);
                DeleteFriendPackage deleteFriendPackage = new DeleteFriendPackage();
                deleteFriendPackage.setFromId(request.getFromId());
                deleteFriendPackage.setToId(request.getToId());
                deleteFriendPackage.setSequence(sequence);
                messageProducer.sendToUser(request.getFromId(), request.getAppId(), request.getClientType(),
                        request.getImei(), FriendshipEventCommand.FRIEND_DELETE, deleteFriendPackage);

                if(configuration.isDeleteFriendAfterCallback()) {
                    DeleteFriendAfterCallbackRequest callbackRequest = new DeleteFriendAfterCallbackRequest();
                    callbackRequest.setFromId(request.getFromId());
                    callbackRequest.setToId(request.getToId());
                    callbackService.callbackAsync(request.getAppId(),
                            Constants.CallbackCommand.DeleteFriendAfter,
                            JSONObject.toJSONString(callbackRequest));
                }
            }
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

        DeleteAllFriendPackage deleteAllFriendPackage = new DeleteAllFriendPackage();
        deleteAllFriendPackage.setFromId(request.getFromId());
        messageProducer.sendToUser(request.getFromId(), request.getAppId(), request.getClientType(),
                request.getImei(), FriendshipEventCommand.FRIEND_ALL_DELETE, deleteAllFriendPackage);

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
        Long sequence = 0L;
        if(friendShip == null) {
            sequence = sequenceGenerator.generate(request + ":" + Constants.SequenceConstants.Friendship);
            FriendShip blackFriendShip = FriendShip.builder()
                                         .fromId(request.getFromId())
                                         .toId(request.getToId())
                                         .appId(request.getAppId())
                                         .friendSequence(sequence)
                                         .black(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode())
                                         .createTime(System.currentTimeMillis())
                                         .build();
            friendShipMapper.insert(blackFriendShip);
            sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Friendship, sequence);
        }else {
            if(friendShip.getBlack() != null && friendShip.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return Result.error(FriendShipErrorCode.FRIEND_IS_BLACK);
            }else {
                sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Friendship);
                FriendShip update = new FriendShip();
                update.setFriendSequence(sequence);
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = friendShipMapper.update(update, wrapper);
                if(result != 1) {
                    return Result.error(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
                sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Friendship, sequence);
            }
        }

        AddFriendBlackPackage addFriendBlackPackage = new AddFriendBlackPackage();
        addFriendBlackPackage.setFromId(request.getFromId());
        addFriendBlackPackage.setToId(request.getToId());
        addFriendBlackPackage.setSequence(sequence);
        messageProducer.sendToUser(request.getFromId(), request.getAppId(), request.getClientType(),
                request.getImei(), FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPackage);

        if(configuration.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackRequest callbackRequest = new AddFriendBlackAfterCallbackRequest();
            callbackRequest.setFromId(request.getFromId());
            callbackRequest.setToId(request.getToId());
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.AddBlackAfter,
                    JSONObject.toJSONString(callbackRequest));
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

        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Friendship);

        FriendShip update = new FriendShip();
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        update.setFriendSequence(sequence);
        int result = friendShipMapper.update(update, wrapper);
        if(result != 1) {
            return Result.error();
        }

        sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Friendship, sequence);

        DeleteBlackPackage deleteBlackPackage = new DeleteBlackPackage();
        deleteBlackPackage.setFromId(request.getFromId());
        deleteBlackPackage.setToId(request.getToId());
        deleteBlackPackage.setSequence(sequence);
        messageProducer.sendToUser(request.getFromId(), request.getAppId(), request.getClientType(),
                request.getImei(), FriendshipEventCommand.FRIEND_BLACK_DELETE, deleteBlackPackage);

        if(configuration.isDeleteFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackRequest callbackRequest = new AddFriendBlackAfterCallbackRequest();
            callbackRequest.setFromId(request.getFromId());
            callbackRequest.setToId(request.getToId());
            callbackService.callbackAsync(request.getAppId(),
                    Constants.CallbackCommand.DeleteBlack,
                    JSONObject.toJSONString(callbackRequest));
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

    @Override
    public Result syncFriendShipList(SyncRequest request) {
        if(request.getLimit() > 100) {
            request.setLimit(100);
        }
        SyncResponse<FriendShip> syncResponse = new SyncResponse<>();
        List<FriendShip> friendShipList = friendShipMapper.selectList(Wrappers.lambdaQuery(FriendShip.class)
                                                                           .eq(FriendShip :: getAppId, request.getAppId())
                                                                           .eq(FriendShip :: getFromId, request.getOperator())
                                                                           .gt(FriendShip :: getFriendSequence, request.getLastSequence())
                                                                           .last("limit" + request.getLimit())
                                                                           .orderByAsc(FriendShip :: getFriendSequence));
        if(CollectionUtil.isNotEmpty(friendShipList)) {
            FriendShip maxFriendShip = friendShipList.get(friendShipList.size() - 1);
            syncResponse.setDataList(friendShipList);
            Long maxSequence = friendShipMapper.getFriendShipMaxSequence(request.getAppId(), request.getOperator());
            syncResponse.setMaxSequence(maxSequence);
            syncResponse.setIsCompleted(maxFriendShip.getFriendSequence() >= maxSequence);
            return Result.success(syncResponse);
        }
        syncResponse.setIsCompleted(true);
        return Result.success(syncResponse);
    }


}
