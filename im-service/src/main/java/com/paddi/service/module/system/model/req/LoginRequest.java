package com.paddi.service.module.system.model.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:42:40
 */
@Data
public class LoginRequest {

    @NotNull(message = "用户id不能位空")
    private String userId;

    @NotNull(message = "appId不能为空")
    private Integer appId;

    private Integer clientType;

}
