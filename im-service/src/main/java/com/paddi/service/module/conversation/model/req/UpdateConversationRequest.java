package com.paddi.service.module.conversation.model.req;

import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月10日 13:34:51
 */
@Data
public class UpdateConversationRequest extends BaseRequest {
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    @NotBlank(message = "发送方ID不能为空")
    private String fromId;

    private Integer isTop;

    private Integer isMute;
}
