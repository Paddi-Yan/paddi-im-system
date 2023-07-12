package com.paddi.codec.pack.user;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UserCustomizedStatusChangeNotifyPackage {

    private String userId;

    private String customizedText;

    private Integer customizedStatus;

}
