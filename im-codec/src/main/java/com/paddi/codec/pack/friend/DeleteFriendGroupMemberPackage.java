package com.paddi.codec.pack.friend;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 删除好友分组成员通知报文
 **/
@Data
public class DeleteFriendGroupMemberPackage {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    /** 序列号*/
    private Long sequence;
}
