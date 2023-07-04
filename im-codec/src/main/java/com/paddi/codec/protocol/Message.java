package com.paddi.codec.protocol;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:29:40
 */
@Data
@ToString
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;
}
