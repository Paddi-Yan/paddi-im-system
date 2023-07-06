package com.paddi.common.enums;

public enum ConnectionStatusEnum {

    /**
     * 管道链接状态,1=在线，0=离线
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(0),
    ;

    private Integer code;

    ConnectionStatusEnum(Integer code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
