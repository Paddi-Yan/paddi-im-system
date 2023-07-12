package com.paddi.service.utils;

import com.paddi.common.model.Result;
import com.paddi.common.utils.HttpUtils;
import com.paddi.service.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月05日 19:06:14
 */
@Component
@Slf4j
public class CallbackService {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private SharedThreadPool sharedThreadPool;

    public void callbackAsync(Integer appId, String callbackCommand, String jsonBody) {
        sharedThreadPool.submit(() -> {
            try {
                httpUtils.doPost(applicationConfiguration.getCallbackUrl(), Object.class, buildUrlParams(appId, callbackCommand), jsonBody, null);
            } catch(Exception e) {
                log.error("callback [{}]:[{}] error=[{}]", callbackCommand, appId, e.getMessage());
            }
        });
    }

    public Result callbackSync(Integer appId, String callbackCommand, String jsonBody) {
        try {
            Result result = httpUtils.doPost("", Result.class, buildUrlParams(appId, callbackCommand), jsonBody, null);
            return result;
        } catch(Exception e) {
            log.error("callback before[{}]:[{}] error=[{}]", callbackCommand, appId, e.getMessage());
            return Result.error();
        }
    }

    public Map<String, Object> buildUrlParams(Integer appId, String command) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("appId", appId);
        paramMap.put("command", command);
        return paramMap;
    }

}
