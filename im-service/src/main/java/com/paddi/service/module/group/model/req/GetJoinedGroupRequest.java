package com.paddi.service.module.group.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.common.model.PageParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 16:07:58
 */
@Data
public class GetJoinedGroupRequest extends BaseRequest {

    @NotBlank(message = "用户id不能为空")
    private String memberId;

    /**
     * 群类型
     */
    private List<Integer> groupType;

    private PageParam pageParam;


}