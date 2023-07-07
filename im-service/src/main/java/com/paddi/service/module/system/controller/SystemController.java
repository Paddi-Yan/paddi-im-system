package com.paddi.service.module.system.controller;

import com.paddi.common.enums.ClientType;
import com.paddi.common.loadbalance.LoadBalance;
import com.paddi.common.model.Result;
import com.paddi.common.utils.RouteInfoParseUtil;
import com.paddi.service.manager.ZookeeperManager;
import com.paddi.service.module.system.model.req.LoginRequest;
import com.paddi.service.module.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 22:16:14
 */
@RestController
@RequestMapping("/v1/system")
public class SystemController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private LoadBalance loadBalance;
    
    @Autowired
    private ZookeeperManager zookeeperManager;


    @PostMapping("/access")
    public Result accessService(@RequestBody @Validated LoginRequest request, Integer appId) {
        request.setAppId(appId);
        Result result = systemService.authenticate(request);
        if(result.isSuccess()) {
            List<String> serverList;
            if(request.getClientType().equals(ClientType.WEB.getCode())) {
                serverList = zookeeperManager.getAllWebSocketNode();
            }else {
                serverList = zookeeperManager.getAllTcpNode();
            }
            String address = loadBalance.selectServiceAddress(serverList, request.getUserId());
            return Result.success(RouteInfoParseUtil.parse(address));
        }
        return Result.error();
    }

    @GetMapping("/signature")
    public Result getSignature(@RequestParam Integer appId, @RequestParam String userId) {
        return Result.success(systemService.getSignature(appId, userId));
    }

}
