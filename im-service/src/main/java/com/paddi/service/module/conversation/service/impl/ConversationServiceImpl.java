package com.paddi.service.module.conversation.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.paddi.codec.pack.conversation.DeleteConversationPackage;
import com.paddi.codec.pack.conversation.UpdateConversationPackage;
import com.paddi.common.constants.Constants;
import com.paddi.common.enums.ConversationErrorCode;
import com.paddi.common.enums.ConversationTypeEnum;
import com.paddi.common.enums.command.ConversationEventCommand;
import com.paddi.common.exception.ApplicationException;
import com.paddi.common.model.ClientInfo;
import com.paddi.common.model.Result;
import com.paddi.common.model.message.MessageReadContent;
import com.paddi.common.model.message.SyncRequest;
import com.paddi.common.model.message.SyncResponse;
import com.paddi.service.config.ApplicationConfiguration;
import com.paddi.service.module.conversation.entity.Conversation;
import com.paddi.service.module.conversation.mapper.ConversationMapper;
import com.paddi.service.module.conversation.model.req.DeleteConversationRequest;
import com.paddi.service.module.conversation.model.req.UpdateConversationRequest;
import com.paddi.service.module.conversation.service.ConversationService;
import com.paddi.service.utils.DataSequenceUtils;
import com.paddi.service.utils.MessageProducer;
import com.paddi.service.utils.RedisSequenceGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 12:49:53
 */
@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private RedisSequenceGenerator sequenceGenerator;

    @Autowired
    private DataSequenceUtils sequenceUtils;


    @Override
    public String generateConversationId(Integer type, String fromId, String toId) {
        return type + "-" + fromId + "-" + toId;
    }

    @Override
    public void messageMarkRead(MessageReadContent messageReadContent) {
        String conversationId = null;
        String toId = null;
        if(messageReadContent.getConversationType().equals(ConversationTypeEnum.P2P.getCode())) {
            conversationId = generateConversationId(messageReadContent.getConversationType(), messageReadContent.getFromId(), messageReadContent.getToId());
            toId = messageReadContent.getToId();
        } else if(messageReadContent.getConversationType().equals(ConversationTypeEnum.GROUP.getCode())) {
            conversationId = generateConversationId(messageReadContent.getConversationType(), messageReadContent.getFromId(), messageReadContent.getGroupId());
            toId = messageReadContent.getGroupId();
        }
        Conversation conversation = conversationMapper.selectOne(Wrappers.lambdaQuery(Conversation.class)
                                                                         .eq(Conversation :: getConversationId, conversationId)
                                                                         .eq(Conversation :: getAppId, messageReadContent.getAppId()));
        Long sequence = sequenceGenerator.generate(messageReadContent.getAppId() + ":" + Constants.SequenceConstants.Conversation);

        if(conversation == null) {
            conversation = new Conversation();
            BeanUtils.copyProperties(messageReadContent, conversation);
            conversation.setToId(toId);
            conversation.setConversationId(conversationId);
            conversation.setSequence(sequence);
            conversation.setReadSequence(messageReadContent.getMessageSequence());
            conversationMapper.insert(conversation);
        }else {
            conversation.setReadSequence(messageReadContent.getMessageSequence());
            conversation.setSequence(sequence);
            conversationMapper.readMark(conversation);
        }

        sequenceUtils.writeSequence(messageReadContent.getAppId(), messageReadContent.getFromId(), Constants.SequenceConstants.Conversation, sequence);
    }

    /**
     * 删除会话,并不是删除数据库中的会话记录也不是删除数据库的消息记录
     * 而是删除APP本地的聊天记录
     * @param request
     * @return
     */
    @Override
    public Result deleteConversation(DeleteConversationRequest request) {
        LambdaQueryWrapper<Conversation> wrapper = Wrappers.lambdaQuery(Conversation.class)
                                                      .eq(Conversation :: getConversationId, request.getConversationId())
                                                      .eq(Conversation :: getAppId, request.getAppId());

        Conversation conversation = conversationMapper.selectOne(wrapper);
        if(conversation == null) {
            throw new ApplicationException(ConversationErrorCode.CONVERSATION_NOT_EXIST);
        }

        Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Conversation);

        conversation.setIsMute(0);
        conversation.setIsTop(0);
        conversation.setSequence(sequence);
        conversationMapper.update(conversation, wrapper);

        sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Conversation, sequence);

        //是否需要同步给其他端
        if(configuration.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPackage deleteConversationPackage = new DeleteConversationPackage();
            deleteConversationPackage.setConversationId(request.getConversationId());
            deleteConversationPackage.setSequence(sequence);
            messageProducer.sendToOtherUserTerminal(request.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    deleteConversationPackage,
                    new ClientInfo(request.getAppId(),
                            request.getClientType(),
                            request.getImei()));
        }
        return Result.success();
    }

    @Override
    public Result updateConversation(UpdateConversationRequest request) {
        if(request.getIsTop() == null && request.getIsMute() == null) {
            return Result.error(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        LambdaQueryWrapper<Conversation> wrapper = Wrappers.lambdaQuery(Conversation.class)
                                                           .eq(Conversation :: getConversationId, request.getConversationId())
                                                           .eq(Conversation :: getAppId, request.getAppId());
        Conversation conversation = conversationMapper.selectOne(wrapper);
        if(conversation != null) {
            if(request.getIsMute() != null) {
                conversation.setIsMute(request.getIsMute());
            }
            if(request.getIsTop() != null) {
                conversation.setIsTop(request.getIsTop());
            }
            Long sequence = sequenceGenerator.generate(request.getAppId() + ":" + Constants.SequenceConstants.Conversation);

            conversationMapper.update(conversation, wrapper);

            sequenceUtils.writeSequence(request.getAppId(), request.getFromId(), Constants.SequenceConstants.Conversation, sequence);

            UpdateConversationPackage updateConversationPackage = new UpdateConversationPackage();
            updateConversationPackage.setConversationId(request.getConversationId());
            updateConversationPackage.setConversationType(conversation.getConversationType());
            updateConversationPackage.setIsMute(conversation.getIsMute());
            updateConversationPackage.setIsTop(conversation.getIsTop());
            updateConversationPackage.setSequence(sequence);
            messageProducer.sendToOtherUserTerminal(request.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    updateConversationPackage,
                    new ClientInfo(request.getAppId(),
                            request.getClientType(),
                            request.getImei()));
        }
        return Result.success();
    }

    @Override
    public Result syncConversationList(SyncRequest request) {
        if(request.getLimit() > 100) {
            request.setLimit(100);
        }

        SyncResponse<Conversation> syncResponse = new SyncResponse<>();
        List<Conversation> conversationList = conversationMapper.selectList(Wrappers.lambdaQuery(Conversation.class)
                                                                                 .eq(Conversation :: getAppId, request.getAppId())
                                                                                 .eq(Conversation :: getFromId, request.getOperator())
                                                                                 .gt(Conversation :: getSequence, request.getLastSequence())
                                                                                 .last("limit" + request.getLimit())
                                                                                 .orderByAsc(Conversation :: getSequence));
        if(CollectionUtil.isNotEmpty(conversationList)) {
            Conversation maxSequenceConversation = conversationList.get(conversationList.size() - 1);
            Long maxSequence = conversationMapper.getMaxSequence(request.getAppId(), request.getOperator());
            syncResponse.setDataList(conversationList);
            syncResponse.setMaxSequence(maxSequence);
            syncResponse.setIsCompleted(maxSequenceConversation.getSequence() >= maxSequence);
            return Result.success(syncResponse);
        }
        syncResponse.setIsCompleted(true);
        return Result.success(syncResponse);
    }
}
