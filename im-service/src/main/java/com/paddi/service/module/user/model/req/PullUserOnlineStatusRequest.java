package com.paddi.service.module.user.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 21:49:51
 */
@Data
public class PullUserOnlineStatusRequest extends BaseRequest {

    private List<String> userList;
}
