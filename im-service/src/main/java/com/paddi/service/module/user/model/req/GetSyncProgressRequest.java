package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 00:14:21
 */
@Data
public class GetSyncProgressRequest extends BaseRequest {
    private String userId;
}
