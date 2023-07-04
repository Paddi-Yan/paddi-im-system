package com.paddi.service.module.friendship.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 15:11:09
 */
@Data
public class AddFriendShipBlackRequest extends BaseRequest {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private String toId;
}
