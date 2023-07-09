package com.paddi.codec.pack.message;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 18:24:35
 */
@Data
public class MessageReceiveServerACKPackage {
    private Long messageKey;
    private String fromId;
    private String toId;
    private Long messageSequence;
    private Boolean sendFromServer;
}