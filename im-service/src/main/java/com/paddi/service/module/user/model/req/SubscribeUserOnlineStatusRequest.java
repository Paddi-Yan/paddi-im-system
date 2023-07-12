package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 20:55:10
 */
@Data
public class SubscribeUserOnlineStatusRequest extends BaseRequest {
    private List<String> subscribedUserId;

    private Long subscribeTime;
}
