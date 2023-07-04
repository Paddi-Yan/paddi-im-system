package com.paddi.service.module.friendship.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 17:44:07
 */
@Data
@TableName("im_friendship_group")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendShipGroup {
    @TableId(value = "group_id",type = IdType.AUTO)
    private Long groupId;

    private String fromId;

    private Integer appId;

    private String groupName;

    private Long createTime;

    private Long updateTime;

    /** 序列号*/
    private Long sequence;

    private int delFlag;
}
