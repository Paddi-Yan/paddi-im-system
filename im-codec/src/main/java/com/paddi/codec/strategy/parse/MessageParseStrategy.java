package com.paddi.codec.strategy.parse;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:46:37
 */
public interface MessageParseStrategy<T> {
    T parse(byte[] bytes);
}
