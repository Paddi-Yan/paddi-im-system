package com.paddi.common.model;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:29:30
 */
@Data
public class UserSession {
    private String userId;

    private Integer appId;

    private Integer clientType;

    /**
     * SDK Version
     */
    private Integer version;

    /**
     * 连接状态 1-在线 0-离线
     */
    private Integer connectionState;

    /**
     * 用户登录的时候记录登录在哪台Netty服务器
     */
    private Integer brokerId;

    private String brokerHost;
}
