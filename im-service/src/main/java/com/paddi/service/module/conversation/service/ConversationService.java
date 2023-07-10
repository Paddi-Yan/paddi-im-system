package com.paddi.service.module.conversation.service;

import com.paddi.common.model.Result;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.service.module.conversation.model.req.DeleteConversationRequest;
import com.paddi.service.module.conversation.model.req.UpdateConversationRequest;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 00:55:09
 */
public interface ConversationService {

    String generateConversationId(Integer type, String fromId, String toId);

    void messageMarkRead(MessageReadContent messageReadContent);

    Result deleteConversation(DeleteConversationRequest request);

    Result updateConversation(UpdateConversationRequest request);

    Result syncConversationList(SyncRequest request);
}
