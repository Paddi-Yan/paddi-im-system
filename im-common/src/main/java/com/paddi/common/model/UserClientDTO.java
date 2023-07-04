package com.paddi.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:59:37
 */
@Data
@AllArgsConstructor
public class UserClientDTO {
    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;
}
