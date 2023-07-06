package com.paddi.codec.pack.friend;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 14:35:34
 */
@Data
public class AddFriendPackage {
    private String fromId;

    /**
     * 备注
     */
    private String remark;

    private String toId;
    /**
     * 好友来源
     */
    private String addSource;
    /**
     * 添加好友时的描述信息（用于打招呼）
     */
    private String addWording;

    private Long sequence;
}
