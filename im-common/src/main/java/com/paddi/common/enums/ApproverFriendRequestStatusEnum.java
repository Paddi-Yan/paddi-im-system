package com.paddi.common.enums;

public enum ApproverFriendRequestStatusEnum {

    /**
     * 0 未处理 1 同意；2 拒绝。
     */
    UNDISPOSED(0),
    AGREE(1),

    REJECT(2),
    ;

    private int code;

    ApproverFriendRequestStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
