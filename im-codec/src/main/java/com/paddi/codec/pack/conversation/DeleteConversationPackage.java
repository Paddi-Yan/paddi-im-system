package com.paddi.codec.pack.conversation;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class DeleteConversationPackage {

    private String conversationId;

    private Long sequence;

}
