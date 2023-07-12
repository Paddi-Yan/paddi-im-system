package com.paddi.common.model.message;

import com.paddi.common.model.ClientInfo;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月12日 14:06:53
 */
@Data
public class RecallMessageContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;

    private Integer conversationType;


        /*{
            "messageKey":419455774914383872,
                "fromId":"lld",
                "toId":"lld4",
                "messageTime":"1665026849851",
                "messageSequence":2,
                "appId": 10000,
                "clientType": 1,
                "imei": "web",
                "conversationType":0
        }*/
}