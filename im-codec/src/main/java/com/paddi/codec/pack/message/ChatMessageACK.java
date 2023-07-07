package com.paddi.codec.pack.message;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class ChatMessageACK {

    private String messageId;
    private Long messageSequence;

    public ChatMessageACK(String messageId) {
        this.messageId = messageId;
    }

    public ChatMessageACK(String messageId, Long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }

}
