package com.paddi.service.module.conversation.controller;

import com.paddi.common.model.Result;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.service.module.conversation.model.req.DeleteConversationRequest;
import com.paddi.service.module.conversation.model.req.UpdateConversationRequest;
import com.paddi.service.module.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 13:22:34
 */
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @DeleteMapping("/delete")
    public Result deleteConversation(@RequestBody DeleteConversationRequest request) {
        return conversationService.deleteConversation(request);
    }

    @DeleteMapping("/update")
    public Result updateConversation(@RequestBody UpdateConversationRequest request) {
        return conversationService.updateConversation(request);
    }

    @GetMapping("syncConversationList")
    public Result syncConversationList(@RequestBody SyncRequest request) {
        return conversationService.syncConversationList(request);
    }
}
