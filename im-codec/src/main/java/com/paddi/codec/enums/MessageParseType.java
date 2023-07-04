package com.paddi.codec.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:37:35
 */
@AllArgsConstructor
@Getter
public enum MessageParseType {

    JSON(0x0, "json"),
    PROTOBUF(0x1, "protobuf"),
    XML(0x2, "xml"),

    ;

    private Integer value;

    private String type;

    private static Map<Integer, MessageParseType> map = new HashMap<>();
    static {
        map.put(0x0, JSON);
        map.put(0x1, PROTOBUF);
        map.put(0x2, XML);
    }

    public static MessageParseType getType(Integer value) {
        return map.get(value);
    }
}
