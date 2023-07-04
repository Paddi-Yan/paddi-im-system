package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import com.paddi.service.module.user.entity.po.User;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月29日 21:20:52
 */
@Data
public class ImportUserRequest extends BaseRequest {

    private List<User> userList;

}
