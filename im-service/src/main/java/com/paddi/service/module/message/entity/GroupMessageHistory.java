package com.paddi.service.module.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:41:45
 */
@Data
@TableName("im_group_message_history")
public class GroupMessageHistory {
    private Integer appId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    private Long messageKey;

    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;
}
