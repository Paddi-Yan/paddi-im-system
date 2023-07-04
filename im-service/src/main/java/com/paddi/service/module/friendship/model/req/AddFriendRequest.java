package com.paddi.service.module.friendship.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:19:28
 */
@Data
public class AddFriendRequest extends BaseRequest {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "toItem不能为空")
    private FriendDTO toItem;
}
