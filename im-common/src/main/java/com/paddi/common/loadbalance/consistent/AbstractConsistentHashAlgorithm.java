package com.paddi.common.loadbalance.consistent;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 17:13:03
 */
public abstract class AbstractConsistentHashAlgorithm {

    /**
     * 向哈希环中添加节点
     * 需要根据不同的数据结构作不同实现
     * @param key
     * @param value
     */
    abstract void add(Long key, String value);

    void sort(){ }

    /**
     * 获取到当前key映射到哈希环顺时针第一个节点
     * @param key
     * @return
     */
    abstract String getFirstNodeValue(String key);

    /**
     * 前置处理
     */
    abstract void processBefore();

    public synchronized String process(List<String> values, String key) {
        processBefore();
        for(String value : values) {
            add(hash(value), value);
        }
        sort();
        return getFirstNodeValue(key);
    }

    public Long hash(String value){
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        long truncateHashCode = hashCode & 0xffffffffL;
        return truncateHashCode;
    }
}
