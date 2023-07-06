package com.paddi.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 修改群成员通知报文
 **/
@Data
public class UpdateGroupMemberPackage {

    private String groupId;

    private String memberId;

    private String alias;

    private String extra;
}
