package com.paddi.codec.pack.user;

import com.paddi.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotifyPackage {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
