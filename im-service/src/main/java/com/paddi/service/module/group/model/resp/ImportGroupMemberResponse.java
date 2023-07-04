package com.paddi.service.module.group.model.resp;

import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:26:55
 */
@Data
public class ImportGroupMemberResponse {

    private String memberId;

    /**
     * 加人结果：0 为成功；1 为失败；2 为已经是群成员
     */
    private Integer result;

    private String resultMessage;
}
