package com.paddi.codec.pack.friend;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 删除好友分组通知报文
 **/
@Data
public class DeleteFriendGroupPackage {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
