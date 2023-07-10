package com.paddi.service.module.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.conversation.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 12:49:25
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    void readMark(Conversation conversation);

    Long getMaxSequence(@Param("appId") Integer appId, @Param("userId") String userId);
}
