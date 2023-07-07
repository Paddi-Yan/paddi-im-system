package com.paddi.service.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.ApplicationExceptionEnum;
import com.paddi.common.enums.BaseErrorCode;
import com.paddi.common.enums.GateWayErrorCode;
import com.paddi.common.utils.SignatureUtils;
import com.paddi.service.module.system.entity.po.AppSignRecord;
import com.paddi.service.module.system.service.SystemService;
import com.paddi.service.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.paddi.common.constants.Constants.UserSignConstants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 13:26:01
 */
@Component
@Slf4j
public class IdentityChecker {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public ApplicationExceptionEnum checkUserSign(String identifier, Integer appId, String userSign) {
        String signCacheKey = appId + Constants.RedisConstants.USER_SIGN + identifier + userSign;
        String cacheUserSign = redisTemplate.opsForValue().get(signCacheKey);
        if(StrUtil.isNotEmpty(cacheUserSign) && Long.valueOf(cacheUserSign) > getCurrentTime()) {
            return BaseErrorCode.SUCCESS;
        }


        AppSignRecord appSignRecord = systemService.getAppSignRecord(appId);
        //获取密钥
        String secretKey = appSignRecord.getSecretKey();
        //解密
        JSONObject signDoc = SignatureUtils.decryptSignature(userSign);
        Long signTime = 0L;
        Long expire = 0L;
        Integer decryptAppId = null;
        String decryptIdentifier = null;

        try {
            decryptAppId = signDoc.getInteger(TLS_APPID);
            decryptIdentifier = signDoc.getString(TLS_IDENTIFIER);
            signTime = signDoc.getLong(TLS_SIGN_TIME);
            expire = signDoc.getLong(TLS_EXPIRE);
        } catch(Exception e) {
            log.warn("签名解析失败: [{}]", signDoc);
        }

        if(!decryptIdentifier.equals(identifier)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }
        if(!decryptAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }
        Long expireTime = signTime + expire;
        if(expire == 0L || expireTime <= getCurrentTime()) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        redisTemplate.opsForValue().set(signCacheKey, expireTime.toString(), expireTime - getCurrentTime(), TimeUnit.SECONDS);
        return BaseErrorCode.SUCCESS;
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

}
