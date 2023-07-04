package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 15:14:05
 */
@Data
public class GetGroupRequest extends BaseRequest {

    private String groupId;

}
