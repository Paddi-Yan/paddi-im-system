package com.paddi.service.module.friendship.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:12:45
 */
@Data
@TableName("im_friendship")
@AutoMap
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendShip {

    private Integer appId;

    private String fromId;

    private String toId;
    /** 备注*/
    private String remark;
    /** 状态 1正常 2删除*/
    private Integer status;
    /** 状态 1正常 2拉黑*/
    private Integer black;
    private Long createTime;
    /** 好友关系序列号*/
    private Long friendSequence;

    /** 黑名单关系序列号*/
    private Long blackSequence;
    /** 好友来源*/
    private String addSource;

    private String extra;
}
