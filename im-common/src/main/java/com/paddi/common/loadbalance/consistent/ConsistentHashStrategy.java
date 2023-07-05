package com.paddi.common.loadbalance.consistent;

import com.paddi.common.loadbalance.AbstractLoadBalanceStrategy;
import lombok.Setter;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:45:38
 */
@Setter
public class ConsistentHashStrategy extends AbstractLoadBalanceStrategy {

    private AbstractConsistentHashAlgorithm algorithm;

    @Override
    protected String doSelect(List<String> addressList, String key) {
        return algorithm.process(addressList, key);
    }
}
