package com.paddi.common.model;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 18:10:11
 */
@Data
public class BaseRequest {

    private Integer appId;

    private String operator;

    private Integer clientType;

    private String imei;
}
