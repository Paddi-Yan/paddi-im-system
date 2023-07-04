package com.paddi.codec.strategy.parse;

import com.alibaba.fastjson.JSON;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:48:33
 */
public class JSONMessageParseStrategy<T> implements MessageParseStrategy<T> {
    @Override
    public T parse(byte[] bytes) {
        String str = new String(bytes);
        return (T) JSON.parseObject(str);
    }
}
