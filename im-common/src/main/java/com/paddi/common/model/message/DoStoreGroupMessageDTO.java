package com.paddi.common.model.message;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 23:28:42
 */
@Data
public class DoStoreGroupMessageDTO {
    private GroupChatMessageContent messageContent;

    private StoredMessageBody storedMessageBody;

}
