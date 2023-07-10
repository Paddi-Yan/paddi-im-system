package com.paddi.common.enums;

/**
 * @author: Chackylee
 * @description:
 **/
public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000,"会话修改参数错误"),
    CONVERSATION_NOT_EXIST(50001,"会话不存在"),


    ;

    private int code;
    private String error;

    ConversationErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getError() {
        return this.error;
    }

}
