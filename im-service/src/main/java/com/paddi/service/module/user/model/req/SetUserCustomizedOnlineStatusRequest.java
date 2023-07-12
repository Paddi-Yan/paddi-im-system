package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 21:33:16
 */
@Data
public class SetUserCustomizedOnlineStatusRequest extends BaseRequest {
    private String userId;

    private String customizedText;

    private Integer customizedStatus;
}
