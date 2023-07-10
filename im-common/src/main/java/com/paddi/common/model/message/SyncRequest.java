package com.paddi.common.model.message;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 21:34:34
 */
@Data
public class SyncRequest extends BaseRequest {

    private Long lastSequence;

    private Integer limit;
}
