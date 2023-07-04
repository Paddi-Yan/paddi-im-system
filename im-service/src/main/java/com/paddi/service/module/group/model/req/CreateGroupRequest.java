package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:47:21
 */
@Data
public class CreateGroupRequest extends BaseRequest {

    private String groupId;
    /**
     * 群主id
     */
    private String ownerId;

    /**
     * 群类型 1私有群（类似微信） 2公开群(类似qq）
     */
    private Integer groupType;

    private String groupName;

    /**
     * 是否全员禁言，0 不禁言；1 全员禁言。
     */
    private Integer mute;
    /**
     * 加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。
     */
    private Integer applyJoinType;

    /**
     * 群简介
     */
    private String introduction;

    /**
     * 群公告
     */
    private String notification;

    /**
     * 群头像
     */
    private String photo;

    private Integer MaxMemberCount;

    private List<GroupMemberDTO> member;

    private String extra;

}
