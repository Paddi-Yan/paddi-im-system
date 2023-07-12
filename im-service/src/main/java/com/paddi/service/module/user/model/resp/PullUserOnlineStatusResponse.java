package com.paddi.service.module.user.model.resp;

import com.paddi.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月12日 12:51:28
 */
@Data
public class PullUserOnlineStatusResponse {
    private List<UserSession> sessions;

    private String customizedText;

    private Integer customizedStatus;
}
