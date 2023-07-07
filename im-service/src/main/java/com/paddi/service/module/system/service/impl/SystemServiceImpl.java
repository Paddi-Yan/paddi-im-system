package com.paddi.service.module.system.service.impl;

import com.paddi.common.enums.BaseErrorCode;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.Result;
import com.paddi.common.utils.SignatureUtils;
import com.paddi.service.module.system.entity.po.AppSignRecord;
import com.paddi.service.module.system.mapper.AppSignRecordMapper;
import com.paddi.service.module.system.model.req.LoginRequest;
import com.paddi.service.module.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:45:00
 */
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private AppSignRecordMapper appSignRecordMapper;
    private static final Long expire = 60 * 60 * 12L;

    @Override
    public Result authenticate(LoginRequest request) {
        return Result.success();
    }

    @Override
    public AppSignRecord getAppSignRecord(Integer appId) {
        return appSignRecordMapper.selectById(appId);
    }

    @Override
    public String getSignature(Integer appId, String userId) {
        AppSignRecord appSignRecord = getAppSignRecord(appId);
        if(appSignRecord == null) {
            throw new ApplicationException(BaseErrorCode.NOT_EXIT_APPID);
        }
        return SignatureUtils.generateSignature(appId, appSignRecord.getSecretKey(), userId, expire);
    }

}
