package com.paddi.common.model.message;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月08日 23:08:43
 */
@Data
public class GroupChatMessageContent extends MessageContent {
    private String groupId;

}
