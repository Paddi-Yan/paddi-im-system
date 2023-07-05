package com.paddi.service.module.friendship.model.callback;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 23:30:19
 */
@Data
public class DeleteFriendAfterCallbackRequest {
    private String fromId;

    private String toId;
}
