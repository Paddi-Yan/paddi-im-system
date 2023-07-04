package com.paddi.common.model;

import com.paddi.common.enums.ApplicationExceptionEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月15日 14:59:48
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -1628626440192735356L;
    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final String DEFAULT_SUCCESS_MSG = "success";

    @Getter
    @Setter
    private int code = 0;

    @Getter
    @Setter
    private String msg = DEFAULT_SUCCESS_MSG;

    @Getter
    @Setter
    private T data;

    public Result() {
        super();
    }

    public Result(T data) {
        super();
        this.data = data;
    }

    public Result(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    public Result(Throwable e) {
        super();
        this.msg = e.getMessage();
        this.code = ERROR_CODE;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(String msg, int code) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> errorData(T data) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> successData(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(DEFAULT_SUCCESS_MSG);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T t) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(DEFAULT_SUCCESS_MSG);
        result.setData(t);
        return result;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(DEFAULT_SUCCESS_MSG);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> successMsg(String msg) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(msg);
        return result;
    }

    public Result(T data, String msg, int code) {
        super();
        this.data = data;
        this.msg = msg;
        this.code = code;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setData(null);
        return result;
    }

    public static Result error(ApplicationExceptionEnum exceptionEnum) {
        return new Result(exceptionEnum.getCode(), exceptionEnum.getError());
    }

    public static Result error() {
        return new Result(500, "系统内部异常", null);
    }

    public boolean hasError() {
        return this.code != SUCCESS_CODE;
    }

    public boolean isSuccess() {
        return this.code == 200;
    }
}
