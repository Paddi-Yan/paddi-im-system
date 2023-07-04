package com.paddi.service.module.friendship.entity.dto;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:21:51
 */
@Data
public class FriendDTO {
    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    private String addWording;
}
