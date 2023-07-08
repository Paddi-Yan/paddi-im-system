package com.paddi.common.model.message;

import com.paddi.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:00:43
 */
@Data
public class MessageContent extends ClientInfo {
    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

    private Long messageTime;

    private String extra;

    private Long messageKey;

    private long messageSequence;
}
