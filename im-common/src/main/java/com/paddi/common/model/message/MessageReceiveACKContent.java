package com.paddi.common.model.message;

import com.paddi.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 17:42:45
 */
@Data
public class MessageReceiveACKContent extends ClientInfo {
    private Long messageKey;
    private String fromId;
    private String toId;
    private Long messageSequence;

}
