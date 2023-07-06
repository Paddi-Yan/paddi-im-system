package com.paddi.codec.pack.friend;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 用户添加黑名单以后tcp通知数据包
 **/
@Data
public class AddFriendBlackPackage {
    private String fromId;

    private String toId;

    private Long sequence;
}
