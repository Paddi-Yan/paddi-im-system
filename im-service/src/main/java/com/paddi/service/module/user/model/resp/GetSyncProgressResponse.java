package com.paddi.service.module.user.model.resp;

import lombok.Data;

import java.util.Map;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月11日 00:23:14
 */
@Data
public class GetSyncProgressResponse {
    private Map<Object, Object> syncProgressSequenceMap;
    public GetSyncProgressResponse(Map<Object, Object> syncProgressSequenceMap) {
        this.syncProgressSequenceMap = syncProgressSequenceMap;
    }
}
