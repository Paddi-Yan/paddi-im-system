package com.paddi.service.module.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:15:26
 */
@Data
public class ImportFriendShipResponse {
    private List<String> successId;

    private List<String> errorId;
}
