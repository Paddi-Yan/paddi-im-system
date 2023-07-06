package com.paddi.codec.pack.friend;

import lombok.Data;


/**
 * @author: Chackylee
 * @description: 修改好友通知报文
 **/
@Data
public class UpdateFriendPackage {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;
}
