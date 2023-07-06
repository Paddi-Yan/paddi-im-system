package com.paddi.service.module.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.friend.ApproveFriendRequestPackage;
import com.paddi.codec.pack.friend.ReadAllFriendRequestPack;
import com.paddi.common.enums.ApproverFriendRequestStatusEnum;
import com.paddi.common.enums.FriendRequestReadStatusEnum;
import com.paddi.common.enums.FriendShipErrorCode;
import com.paddi.common.enums.command.FriendshipEventCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.BaseRequest;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import com.paddi.service.module.friendship.entity.po.FriendShipRequest;
import com.paddi.service.module.friendship.mapper.FriendShipRequestMapper;
import com.paddi.service.module.friendship.model.req.ApproveFriendRequestReq;
import com.paddi.service.module.friendship.model.req.ReadFriendShipRequestReq;
import com.paddi.service.module.friendship.service.FriendShipRequestService;
import com.paddi.service.module.friendship.service.FriendShipService;
import com.paddi.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 15:57:11
 */
@Service
public class FriendShipRequestServiceImpl implements FriendShipRequestService {

    @Autowired
    private FriendShipRequestMapper friendShipRequestMapper;

    @Autowired
    private FriendShipService friendShipService;

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public Result addFriendShipRequest(BaseRequest baseRequest, String fromId, FriendDTO friendDTO, Integer appId) {
        LambdaQueryWrapper<FriendShipRequest> wrapper = Wrappers.lambdaQuery(FriendShipRequest.class)
                                                           .eq(FriendShipRequest :: getFromId, fromId)
                                                           .eq(FriendShipRequest :: getToId, friendDTO.getToId())
                                                           .eq(FriendShipRequest :: getAppId, appId);
        FriendShipRequest request = friendShipRequestMapper.selectOne(wrapper);
        if(request == null) {
            request = FriendShipRequest.builder()
                               .addSource(friendDTO.getAddSource())
                               .addWording(friendDTO.getAddWording())
                               .appId(appId)
                               .fromId(fromId)
                               .toId(friendDTO.getToId())
                               .readStatus(FriendRequestReadStatusEnum.UNREAD.getCode())
                               .approveStatus(ApproverFriendRequestStatusEnum.UNDISPOSED.getCode())
                               .remark(friendDTO.getRemark())
                               .createTime(System.currentTimeMillis())
                               .build();
            friendShipRequestMapper.insert(request);
        }else {
            //修改记录内容 和更新时间
            if(StringUtils.isNotBlank(friendDTO.getAddSource())){
                request.setAddWording(friendDTO.getAddWording());
            }
            if(StringUtils.isNotBlank(friendDTO.getRemark())){
                request.setRemark(friendDTO.getRemark());
            }
            if(StringUtils.isNotBlank(friendDTO.getAddWording())){
                request.setAddWording(friendDTO.getAddWording());
            }
            request.setReadStatus(FriendRequestReadStatusEnum.UNREAD.getCode());
            request.setApproveStatus(ApproverFriendRequestStatusEnum.UNDISPOSED.getCode());
            request.setUpdateTime(System.currentTimeMillis());
            friendShipRequestMapper.updateById(request);
        }

        //发送给被添加的一方
        messageProducer.sendToAllUserTerminal(friendDTO.getToId(), FriendshipEventCommand.FRIEND_REQUEST, request, appId);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result approveFriendRequest(ApproveFriendRequestReq request) {
        FriendShipRequest friendShipRequest = friendShipRequestMapper.selectById(request.getId());
        if(friendShipRequest == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }
        if(!request.getOperator().equals(friendShipRequest.getToId())) {
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }
        FriendShipRequest requestUpdateInfo = new FriendShipRequest();
        requestUpdateInfo.setApproveStatus(request.getStatus());
        requestUpdateInfo.setUpdateTime(System.currentTimeMillis());
        requestUpdateInfo.setId(request.getId());
        friendShipRequestMapper.updateById(requestUpdateInfo);

        if(request.getStatus().equals(ApproverFriendRequestStatusEnum.AGREE.getCode())) {
            FriendDTO friendDTO = new FriendDTO();
            friendDTO.setAddSource(friendShipRequest.getAddSource());
            friendDTO.setAddWording(friendDTO.getAddWording());
            friendDTO.setRemark(friendDTO.getRemark());
            friendDTO.setToId(friendDTO.getToId());
            Result response = friendShipService.doAddFriend(request, friendShipRequest.getFromId(), friendDTO, request.getAppId());
            if(!response.isSuccess() && response.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()) {
                return response;
            }
        }
        ApproveFriendRequestPackage approveFriendRequestPackage = new ApproveFriendRequestPackage();
        approveFriendRequestPackage.setId(request.getId());
        approveFriendRequestPackage.setStatus(request.getStatus());
        messageProducer.sendToOtherUserTerminal(friendShipRequest.getToId(), FriendshipEventCommand.FRIEND_REQUEST_APPROVER, approveFriendRequestPackage,
                new ClientInfo(request.getAppId(), request.getClientType(), request.getImei()));
        return Result.success();
    }

    @Override
    public Result getFriendRequest(String fromId, Integer appId) {
        List<FriendShipRequest> requestList = friendShipRequestMapper.selectList(
                Wrappers.lambdaQuery(FriendShipRequest.class)
                        .eq(FriendShipRequest :: getFromId, fromId)
                        .eq(FriendShipRequest :: getAppId, appId));
        return Result.success(requestList);
    }

    @Override
    public Result readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        FriendShipRequest friendShipRequest = new FriendShipRequest();
        friendShipRequest.setReadStatus(FriendRequestReadStatusEnum.READ.getCode());
        friendShipRequestMapper.update(friendShipRequest,
                Wrappers.lambdaUpdate(FriendShipRequest.class)
                        .eq(FriendShipRequest::getToId, req.getFromId())
                        .eq(FriendShipRequest::getAppId, req.getAppId()));
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        messageProducer.sendToOtherUserTerminal(req.getFromId(), FriendshipEventCommand.FRIEND_REQUEST_READ, readAllFriendRequestPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        return Result.success();
    }
}
