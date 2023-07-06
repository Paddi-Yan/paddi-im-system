package com.paddi.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 14:08:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {
    private Integer appId;

    private Integer clientType;

    private String imei;
}
