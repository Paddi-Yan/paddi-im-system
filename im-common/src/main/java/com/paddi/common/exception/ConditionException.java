package com.paddi.common.exception;

import com.paddi.common.constants.HttpStatus;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月15日 15:18:04
 */
public class ConditionException extends RuntimeException{
    private static final long serialVersionUID = -2522204906733418697L;

    private int code;

    public ConditionException(int code, String name) {
        super(name);
        this.code = code;
    }

    public ConditionException(String name) {
        super(name);
        this.code = HttpStatus.ERROR;
    }

    public int getCode() {
        return code;
    }
}
