package com.paddi.common.loadbalance;

import com.paddi.common.enums.UserErrorCode;
import com.paddi.common.exception.ApplicationException;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:58:51
 */
public abstract class AbstractLoadBalanceStrategy implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> addressList, String key) {
        if(CollectionUtils.isEmpty(addressList)) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        if(addressList.size() == 0) {
            return addressList.get(0);
        }
        return doSelect(addressList, key);
    }

    protected abstract String doSelect(List<String> addressList, String key);
}
