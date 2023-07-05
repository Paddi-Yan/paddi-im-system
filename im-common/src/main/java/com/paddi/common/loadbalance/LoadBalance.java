package com.paddi.common.loadbalance;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:56:40
 */
public interface LoadBalance {

    String selectServiceAddress(List<String> addressList, String key);
}
