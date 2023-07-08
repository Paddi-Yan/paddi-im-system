package com.paddi.service.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.message.entity.MessageHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 00:50:19
 */
@Mapper
public interface MessageHistoryMapper extends BaseMapper<MessageHistory> {
    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<MessageHistory> entityList);
}
