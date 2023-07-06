package com.paddi.service.module.group.model.callback;

import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月06日 09:59:22
 */
@Data
public class AddGroupMemberCallbackRequest {

    private String groupId;

    private String operator;

    private Integer groupType;

    private List<GroupMemberDTO> members;



}
