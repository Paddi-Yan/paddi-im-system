package com.paddi.codec.pack.message;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class MessageReadPackage {

    private Long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;
}
