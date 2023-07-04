package com.paddi.service.module.group.model.resp;

import com.paddi.service.module.group.entity.po.Group;
import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月02日 16:38:24
 */
@Data
public class GetJoinedGroupResponse {

    private Integer totalCount;

    private List<Group> groupList;

}
