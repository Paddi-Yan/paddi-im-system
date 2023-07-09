package com.paddi.common.model.message;

import com.paddi.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 00:33:38
 */
@Data
public class MessageReadContent extends ClientInfo {
    private Long messageSequence;

    private String fromId;

    private String toId;

    /**
     * 会话类型: 私聊/群聊
     */
    private Integer conversationType;
}
