package com.paddi.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 群内添加群成员通知报文
 **/
@Data
public class AddGroupMemberPackage {

    private String groupId;

    private List<String> members;

}
