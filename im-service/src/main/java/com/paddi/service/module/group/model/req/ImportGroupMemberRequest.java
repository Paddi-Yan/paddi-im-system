package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 14:22:23
 */
@Data
public class ImportGroupMemberRequest extends BaseRequest {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private List<GroupMemberDTO> members;

}