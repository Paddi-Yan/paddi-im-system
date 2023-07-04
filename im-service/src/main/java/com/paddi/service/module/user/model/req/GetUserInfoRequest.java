package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:09:59
 */
@Data
public class GetUserInfoRequest extends BaseRequest {
    private List<String> userIds;
}
