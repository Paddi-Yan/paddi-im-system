package com.paddi.service.module.message.service;

import com.paddi.common.model.Result;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 23:04:51
 */
public interface CheckSendMessageService {

    Result checkSenderForbidOrMute(String fromId, Integer appId);

    Result checkFriendShip(String fromId, String toId, Integer appId);

    Result checkGroupMessage(String fromId, String groupId, Integer appId);
}
