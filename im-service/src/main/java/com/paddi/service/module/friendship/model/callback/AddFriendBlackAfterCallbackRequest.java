package com.paddi.service.module.friendship.model.callback;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 23:32:25
 */
@Data
public class AddFriendBlackAfterCallbackRequest {
    private String fromId;

    private String toId;
}
