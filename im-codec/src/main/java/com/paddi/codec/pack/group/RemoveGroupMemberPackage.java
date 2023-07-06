package com.paddi.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 踢人出群通知报文
 **/
@Data
public class RemoveGroupMemberPackage {

    private String groupId;

    private String member;

}
