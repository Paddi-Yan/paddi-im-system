package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:20:15
 */
@Data
public class ModifyUserInfoRequest extends BaseRequest {
    /**
     * 用户id
     */
    @NotEmpty(message = "用户id不能为空")
    private String userId;

    /**
     *
     */
    private String nickName;

    /**
     * 位置
     */
    private String location;

    /**
     * 生日
     */
    private String birthDay;

    private String password;

    /**
     * 头像
     */
    private String photo;

    /**
     * 性别
     */
    private String userSex;

    /**
     * 个性签名
     */
    private String selfSignature;

    /**
     * 加好友验证类型（Friend_AllowType） 1需要验证
     */
    private Integer friendAllowType;

    private String extra;
}
