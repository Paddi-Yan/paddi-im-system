package com.paddi.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 转让群主通知报文
 **/
@Data
public class TransferGroupPackage {

    private String groupId;

    private String ownerId;

    private Long sequence;

}
