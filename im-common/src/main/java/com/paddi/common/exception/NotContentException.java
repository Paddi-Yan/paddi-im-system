package com.paddi.common.exception;

import com.paddi.common.constants.HttpStatus;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月19日 10:02:49
 */
public class NotContentException extends RuntimeException{
    private static final long serialVersionUID = -414757700148821298L;

    private int code;
    public NotContentException(int code, String message) {
        super(message);
        this.code = code;
    }

    public NotContentException(String message) {
        super(message);
        this.code = HttpStatus.NO_CONTENT;
    }

    public NotContentException() {
        super("操作已经执行成功,暂无返回内容,请稍后重试!");
        this.code = HttpStatus.NO_CONTENT;
    }

    public int getCode() {
        return code;
    }

}
