package com.paddi.service.module.user.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:21:27
 */
@Data
@TableName("im_user")
public class User {


    private String userId;


    private String nickName;


    private String location;


    private String birthDay;

    private String password;


    private String photo;


    private Integer gender;


    private String selfSignature;

    /**
     * 加好友验证类型（Friend_AllowType） 1需要验证
     */
    private Integer friendAllowType;

    /**
     * 管理员禁止用户添加加好友：0 未禁用 1 已禁用
     */
    private Integer disableAddFriend;

    /**
     * 禁用标识(0 未禁用 1 已禁用)
     */
    private Integer forbiddenFlag;

    /**
     * 禁言标识
     */
    private Integer silentFlag;

    /**
     * 用户类型 1普通用户 2客服 3机器人
     */
    private Integer userType;

    private Integer appId;

    private Integer delFlag;

    private String extra;

}
