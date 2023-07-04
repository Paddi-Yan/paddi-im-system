package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 16:53:06
 */
@Data
public class DestroyGroupRequest extends BaseRequest {

    @NotNull(message = "群id不能为空")
    private String groupId;

}
