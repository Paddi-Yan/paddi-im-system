package com.paddi.service.module.user.model;

import com.paddi.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 20:23:06
 */
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {
    private String userId;

    /** 1:上线 2:离线 */
    private Integer status;
}
