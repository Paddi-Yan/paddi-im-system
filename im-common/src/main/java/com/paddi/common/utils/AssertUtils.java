package com.paddi.common.utils;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月25日 23:32:10
 */
public class AssertUtils {

    public static <T, E extends RuntimeException> void isNull(T t, E e) {
        if(t == null) {
            throw e;
        }
    }

    public static <T, E extends RuntimeException> void isNull(E e, T... items) {
        for(T t : items) {
            if(t == null) {
                throw e;
            }
        }
    }

    public static <E extends RuntimeException> void isError(Boolean condition, E e) {
        if(condition) {
            throw e;
        }
    }
}
