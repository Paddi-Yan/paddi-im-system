package com.paddi.service.module.group.entity.dto;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:22:48
 */
@Data
public class GroupMemberDTO {
    private String memberId;

    private String alias;

    /**
     * 群成员类型，0 普通成员, 1 管理员, 2 群主， 3 已经移除的成员，当修改群成员信息时，只能取值0/1，其他值由其他接口实现，暂不支持3
     */
    private Integer role;

    //    private Integer speakFlag;

    private Long speakDate;

    private String joinType;

    private Long joinTime;

}
