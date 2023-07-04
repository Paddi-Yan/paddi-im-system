package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 17:48:18
 */
@Data
public class RemoveGroupMemberRequest extends BaseRequest {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private String memberId;

}
