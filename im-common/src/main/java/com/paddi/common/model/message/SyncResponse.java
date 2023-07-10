package com.paddi.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 21:33:43
 */
@Data
public class SyncResponse<T> {
    private Long maxSequence;

    private Boolean isCompleted;

    private List<T> dataList;
}
