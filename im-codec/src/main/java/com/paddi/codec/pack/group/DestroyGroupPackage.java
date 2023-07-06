package com.paddi.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 解散群通知报文
 **/
@Data
public class DestroyGroupPackage {

    private String groupId;

    private Long sequence;

}
