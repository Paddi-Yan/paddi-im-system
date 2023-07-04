package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:16:42
 */
@Data
public class GetSingleUserInfoRequest extends BaseRequest {

    @NotBlank(message = "用户编号不能为空")
    private String userId;
}
