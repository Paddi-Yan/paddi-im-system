package com.paddi.codec.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息服务发送给TCP的包体,TCP再根据包体解析成Message发送给客户端
 *
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 12:30:00
 */
@Data
public class MessagePack<T> implements Serializable {
    private static final long serialVersionUID = -5029939378450218760L;

    private String userId;

    private Integer appId;

    /**
     * 接收方
     */
    private String toId;

    /**
     * 客户端标识
     */
    private int clientType;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 客户端设备唯一标识
     */
    private String imei;

    private Integer command;

    /**
     * 业务数据对象，如果是聊天消息则不需要解析直接透传
     */
    private T data;

}
