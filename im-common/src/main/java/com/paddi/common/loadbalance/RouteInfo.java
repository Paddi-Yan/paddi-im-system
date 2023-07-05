package com.paddi.common.loadbalance;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:18:50
 */
@Data
public final class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}