package com.paddi.service.module.friendship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.friendship.entity.po.FriendShip;
import com.paddi.service.module.friendship.model.req.CheckFriendShipRequest;
import com.paddi.service.module.friendship.model.resp.CheckFriendShipResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:12:20
 */
@Mapper
public interface FriendShipMapper extends BaseMapper<FriendShip> {
    List<CheckFriendShipResponse> checkFriendShip(CheckFriendShipRequest request);

    List<CheckFriendShipResponse> checkFriendShipBoth(CheckFriendShipRequest request);

    List<CheckFriendShipResponse> checkBlackFriendShip(CheckFriendShipRequest request);

    List<CheckFriendShipResponse> checkBlackFriendShipBoth(CheckFriendShipRequest request);
}
