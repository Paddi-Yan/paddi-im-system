package com.paddi.codec.pack.friend;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 删除好友通知报文
 **/
@Data
public class DeleteFriendPackage {

    private String fromId;

    private String toId;

    private Long sequence;
}
