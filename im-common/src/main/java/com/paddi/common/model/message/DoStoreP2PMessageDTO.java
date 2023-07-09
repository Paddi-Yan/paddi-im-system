package com.paddi.common.model.message;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:09:41
 */
@Data
public class DoStoreP2PMessageDTO {
    private MessageContent messageContent;

    private StoredMessageBody storedMessageBody;
}
