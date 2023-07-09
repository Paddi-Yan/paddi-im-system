package com.paddi.feign;

import com.paddi.common.model.Result;
import com.paddi.common.model.message.CheckMessageRequest;
import feign.Headers;
import feign.RequestLine;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 15:27:21
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/check")
    Result checkMessage(CheckMessageRequest request);
}
