package com.paddi.service.module.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:41:35
 */
@Data
@TableName("im_message_history")
public class MessageHistory {
    private Integer appId;

    private String fromId;

    private String toId;

    private String ownerId;

    /** messageBodyId*/
    private Long messageKey;

    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;
}
