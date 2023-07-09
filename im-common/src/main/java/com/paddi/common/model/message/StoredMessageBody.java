package com.paddi.common.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:10:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoredMessageBody {
    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;
}
