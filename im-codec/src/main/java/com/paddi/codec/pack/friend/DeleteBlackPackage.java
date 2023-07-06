package com.paddi.codec.pack.friend;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 删除黑名单通知报文
 **/
@Data
public class DeleteBlackPackage {

    private String fromId;

    private String toId;

    private Long sequence;
}
