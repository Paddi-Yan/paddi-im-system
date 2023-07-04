package com.paddi.service.module.friendship.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 11:19:21
 */
@Data
public class GetAllFriendShipRequest extends BaseRequest {
    @NotBlank(message = "用户id不能为空")
    private String fromId;
}