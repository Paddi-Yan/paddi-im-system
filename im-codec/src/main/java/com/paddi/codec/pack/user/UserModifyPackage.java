package com.paddi.codec.pack.user;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 14:14:39
 */
@Data
public class UserModifyPackage {


    private String userId;


    private String nickName;

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
}
