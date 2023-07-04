package com.paddi.service.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.user.entity.po.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:24:34
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
