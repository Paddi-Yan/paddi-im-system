package com.paddi.common.model.message;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 15:31:28
 */
@Data
public class CheckMessageRequest {
    private String fromId;
    private String toId;
    private Integer appId;
    private Integer command;
}
