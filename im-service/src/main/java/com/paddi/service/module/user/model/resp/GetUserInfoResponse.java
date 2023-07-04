package com.paddi.service.module.user.model.resp;

import com.paddi.service.module.user.entity.po.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 00:14:37
 */
@Data
@AllArgsConstructor
public class GetUserInfoResponse {

    private List<User> userInfoList;

    private List<String> failUserList;

}
