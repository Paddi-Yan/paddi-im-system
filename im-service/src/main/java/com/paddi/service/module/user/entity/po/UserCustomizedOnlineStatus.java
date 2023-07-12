package com.paddi.service.module.user.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 21:37:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCustomizedOnlineStatus {

    private String customizedText;

    private Integer customizedStatus;
}
