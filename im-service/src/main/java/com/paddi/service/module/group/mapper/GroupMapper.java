package com.paddi.service.module.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.group.entity.po.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:12:26
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    Long getMaxSequence(@Param("appId") Integer appId,@Param("list") List<String> groupIdList);
}
