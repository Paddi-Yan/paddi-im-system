package com.paddi.common.loadbalance.round;

import com.paddi.common.loadbalance.AbstractLoadBalanceStrategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:32:13
 */
public class RoundRobinStrategy extends AbstractLoadBalanceStrategy {

    private AtomicLong counter = new AtomicLong();

    @Override
    protected String doSelect(List<String> addressList, String key) {
        long index = counter.incrementAndGet() % addressList.size();
        if((int) index != index) {
            synchronized(RoundRobinStrategy.class) {
                counter = new AtomicLong();
            }
        }
        return addressList.get(Math.toIntExact(index));
    }
}
