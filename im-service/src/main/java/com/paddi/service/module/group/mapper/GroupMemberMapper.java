package com.paddi.service.module.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.paddi.service.module.group.entity.po.GroupMember;
import com.paddi.service.module.group.entity.vo.GroupMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:23:59
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    List<GroupMemberVO> getGroupMember(@Param("groupId") String groupId, @Param("appId") Integer appId);

    List<String> getJoinedGroupIdList(Integer appId, String memberId);

    List<String> getGroupMemberId(@Param("groupId") String groupId, @Param("appId") Integer appId);
}
