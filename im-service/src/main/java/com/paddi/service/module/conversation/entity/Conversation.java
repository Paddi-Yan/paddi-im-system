package com.paddi.service.module.conversation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 12:47:56
 */
@Data
@TableName("im_conversation_set")
public class Conversation {

    /**
     * 会话id 0_fromId_toId
     */
    private String conversationId;

    /**
     * 会话类型
     */
    private Integer conversationType;

    private String fromId;

    private String toId;

    /**
     * 是否免打扰
     */
    private int isMute;

    /**
     * 是否置顶
     */
    private int isTop;

    private Long sequence;

    private Long readSequence;

    private Integer appId;
}
