package com.paddi.service.module.group.model.resp;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 15:31:14
 */
@Data
public class GetMemberRoleResponse {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;

}
