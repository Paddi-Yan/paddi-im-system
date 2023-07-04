package com.paddi.service.module.friendship.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 16:19:41
 */
@Data
public class ApproveFriendRequestReq extends BaseRequest {

    private Long id;

    /** 1同意 2拒绝 */
    @NotNull(message = "处理好友请求类型不能为空")
    @Min(value = 1)
    @Max(value = 2)
    private Integer status;
}
