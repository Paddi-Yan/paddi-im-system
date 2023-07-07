package com.paddi.service.module.system.service;

import com.paddi.common.model.Result;
import com.paddi.service.module.system.entity.po.AppSignRecord;
import com.paddi.service.module.system.model.req.LoginRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:44:55
 */
public interface SystemService {
    Result authenticate(LoginRequest request);

    AppSignRecord getAppSignRecord(Integer appId);

    String getSignature(Integer appId, String userId);
}
