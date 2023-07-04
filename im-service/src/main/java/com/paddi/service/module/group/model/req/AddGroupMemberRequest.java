package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.service.module.group.entity.dto.GroupMemberDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 17:35:18
 */
@Data
public class AddGroupMemberRequest extends BaseRequest {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotEmpty(message = "群成员不能为空")
    private List<GroupMemberDTO> members;

}
