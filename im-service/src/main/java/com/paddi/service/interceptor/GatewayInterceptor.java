package com.paddi.service.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.paddi.common.enums.ApplicationExceptionEnum;
import com.paddi.common.enums.BaseErrorCode;
import com.paddi.common.enums.GateWayErrorCode;
import com.paddi.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 10:29:06
 */
@Component
public class GatewayInterceptor implements HandlerInterceptor {

    @Autowired
    private IdentityChecker identityChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        //TODO 放行验证
        if(1 == 1) {
            return true;
        }

        String appId = request.getHeader("appId");
        if(StrUtil.isEmpty(appId)) {
            errorResponse(Result.error(GateWayErrorCode.APPID_NOT_EXIST), response);
            return false;
        }

        String identifier = request.getHeader("identifier");
        if(StrUtil.isEmpty(identifier)) {
            errorResponse(Result.error(GateWayErrorCode.OPERATER_NOT_EXIST), response);
            return false;
        }

        String userSign = request.getHeader("userSign");
        if(StrUtil.isEmpty(userSign)) {
            errorResponse(Result.error(GateWayErrorCode.USERSIGN_NOT_EXIST), response);
            return false;
        }

        //校验签名是否和appId和userId匹配
        ApplicationExceptionEnum exceptionEnum = identityChecker.checkUserSign(identifier, Integer.valueOf(appId), userSign);
        if(exceptionEnum != BaseErrorCode.SUCCESS) {
             errorResponse(Result.error(exceptionEnum), response);
             return false;
        }

        return true;
    }

    private void errorResponse(Result result, HttpServletResponse response) {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");

        try {
            String resp = JSONObject.toJSONString(result);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Credentials","true");
            response.setHeader("Access-Control-Allow-Methods","*");
            response.setHeader("Access-Control-Allow-Headers","*");
            response.setHeader("Access-Control-Max-Age","3600");

            writer = response.getWriter();
            writer.write(resp);
        }catch(Exception e) {

        }finally {
            if(writer != null) {
                writer.checkError();
            }
        }
    }
}
