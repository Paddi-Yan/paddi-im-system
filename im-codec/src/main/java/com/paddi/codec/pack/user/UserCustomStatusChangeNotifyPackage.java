package com.paddi.codec.pack.user;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UserCustomStatusChangeNotifyPackage {

    private String customText;

    private Integer customStatus;

    private String userId;

}
