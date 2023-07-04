package com.paddi.codec.factory;

import com.paddi.codec.enums.MessageParseType;
import com.paddi.codec.strategy.parse.JSONMessageParseStrategy;
import com.paddi.codec.strategy.parse.MessageParseStrategy;
import com.paddi.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:50:17
 */
public class MessageParseStrategyFactory {

    public static Map<MessageParseType, MessageParseStrategy> INSTANCE = new HashMap<>();
    static {
        INSTANCE.put(MessageParseType.JSON, new JSONMessageParseStrategy());
    }

    public static MessageParseStrategy getInstance(Integer value) {
        MessageParseType messageParseType = MessageParseType.getType(value);
        if(messageParseType == null) {
            throw new BadRequestException("错误的消息解析类型");
        }
        return INSTANCE.get(messageParseType);
    }

}
