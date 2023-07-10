package com.paddi.service.module.message.controller;

import com.paddi.common.enums.BaseErrorCode;
import com.paddi.common.enums.command.GroupEventCommand;
import com.paddi.common.enums.command.MessageCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.CheckMessageRequest;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.service.module.message.service.GroupMessageService;
import com.paddi.service.module.message.service.MessageSyncService;
import com.paddi.service.module.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 15:35:51
 */
@RestController
@RequestMapping("/v1/message")
public class MessageController {

    @Autowired
    private P2PMessageService messageService;

    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private MessageSyncService messageSyncService;

    @PostMapping("/check")
    public Result checkMessage(@RequestBody CheckMessageRequest request) {
        if(request.getCommand() == MessageCommand.MSG_P2P.getCommand()) {
            return messageService.processBefore(request.getFromId(), request.getToId(), request.getAppId());
        }else if(request.getCommand() == GroupEventCommand.MSG_GROUP.getCommand()){
            return groupMessageService.processBefore(request.getFromId(), request.getToId(), request.getAppId());
        }
        throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR);
    }

    @GetMapping("/syncOfflineMessage")
    public Result syncOfflineMessage(@RequestBody SyncRequest request) {
        return messageSyncService.syncOfflineMessage(request);
    }
}
