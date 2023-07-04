package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 23:00:10
 */
@Data
public class DeleteUserRequest extends BaseRequest {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userId;

}
