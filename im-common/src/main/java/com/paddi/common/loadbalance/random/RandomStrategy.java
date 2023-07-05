package com.paddi.common.loadbalance.random;

import com.paddi.common.loadbalance.AbstractLoadBalanceStrategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:00:20
 */
public class RandomStrategy extends AbstractLoadBalanceStrategy {
    @Override
    protected String doSelect(List<String> addressList, String key) {
        int index = ThreadLocalRandom.current().nextInt(addressList.size());
        return addressList.get(index);
    }
}
