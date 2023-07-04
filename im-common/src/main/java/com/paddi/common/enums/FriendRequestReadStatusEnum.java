package com.paddi.common.enums;

public enum FriendRequestReadStatusEnum {

    /**
     * 0未读 1已读
     */
    UNREAD(0),

    READ(1),
    ;

    private int code;

    FriendRequestReadStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
