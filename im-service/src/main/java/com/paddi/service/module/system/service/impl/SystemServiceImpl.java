package com.paddi.service.module.system.service.impl;

import com.paddi.common.model.Result;
import com.paddi.service.module.system.model.req.LoginRequest;
import com.paddi.service.module.system.service.SystemService;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:45:00
 */
@Service
public class SystemServiceImpl implements SystemService {
    @Override
    public Result authenticate(LoginRequest request) {
        return Result.success();
    }
}
