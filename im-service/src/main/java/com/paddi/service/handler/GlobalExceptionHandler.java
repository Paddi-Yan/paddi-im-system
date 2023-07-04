package com.paddi.service.handler;

import com.paddi.common.constants.HttpStatus;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.exception.BadRequestException;
import com.paddi.common.exception.ConditionException;
import com.paddi.common.exception.NotContentException;
import com.paddi.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月15日 15:16:49
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result<String> exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(value = ConditionException.class)
    @ResponseBody
    public Result<String> conditionExceptionHandler(ConditionException e) {
        log.error(e.getMessage());
        return Result.error(e.getMessage(), e.getCode());
    }

    @ExceptionHandler(value = BadRequestException.class)
    @ResponseBody
    public Result<String> badRequestExceptionHandler(BadRequestException e) {
        log.error(e.getMessage());
        return Result.error(e.getMessage(), e.getCode());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public Result validatedBindException(BindException e) {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return Result.error(message, HttpStatus.BAD_REQUEST);
    }


    @ResponseBody
    @ExceptionHandler(NotContentException.class)
    public Result notContentExceptionHandler(NotContentException e) {
        return Result.error(e.getMessage(), e.getCode());
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public Object applicationExceptionHandler(ApplicationException e) {
        // 使用公共的结果类封装返回结果, 这里我指定状态码为
        return Result.error(e.getError(), e.getCode());
    }
}
