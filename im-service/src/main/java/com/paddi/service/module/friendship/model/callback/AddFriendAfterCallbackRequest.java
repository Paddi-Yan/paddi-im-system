package com.paddi.service.module.friendship.model.callback;

import com.paddi.service.module.friendship.entity.dto.FriendDTO;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 23:24:34
 */
@Data
public class AddFriendAfterCallbackRequest {

    private String fromId;

    private FriendDTO toItem;
}
