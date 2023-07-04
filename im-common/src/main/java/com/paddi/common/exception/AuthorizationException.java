package com.paddi.common.exception;

import com.paddi.common.constants.HttpStatus;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月19日 10:02:49
 */
public class AuthorizationException extends RuntimeException{
    private static final long serialVersionUID = -414757700436821298L;

    private int code;
    public AuthorizationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AuthorizationException(String message) {
        super(message);
        this.code = HttpStatus.UNAUTHORIZED;
    }

    public int getCode() {
        return code;
    }

}
