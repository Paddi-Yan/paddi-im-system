package com.paddi.common.loadbalance.consistent;

import com.paddi.common.enums.UserErrorCode;
import com.paddi.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 17:32:57
 */
public class TreeMapConsistentHashAlgorithm extends AbstractConsistentHashAlgorithm {

    private TreeMap<Long, String> treeMap = new TreeMap<>();

    private static final Integer VIRTUAL_NODE_COUNT = 10;

    private static final String VIRTUAL_NODE_PREFIX = "virtual-node-";

    @Override
    void add(Long key, String value) {
        for(int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
            treeMap.put(hash(VIRTUAL_NODE_PREFIX + key + i), value);
        }
        treeMap.put(key, value);
    }

    @Override
    String getFirstNodeValue(String key) {
        Long hash = hash(key);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if(!last.isEmpty()) {
            return last.get(last.firstKey());
        }
        if(treeMap.isEmpty()) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    void processBefore() {
        treeMap.clear();
    }
}
