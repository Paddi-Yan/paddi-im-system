package com.paddi.service.module.friendship.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:57:12
 */
@Data
@TableName("im_friendship_group_member")
public class FriendShipGroupMember {
    @TableId(value = "group_id")
    private Long groupId;

    private String toId;
}
