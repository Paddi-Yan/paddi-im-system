package com.paddi.service.module.friendship.model.req;

import com.paddi.common.enums.FriendShipStatusEnum;
import com.paddi.common.model.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年06月30日 10:06:02
 */
@Data
public class ImportFriendShipRequest extends BaseRequest {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    private List<ImportFriendDTO> friendItem;

    @Data
    public static class ImportFriendDTO{

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();

        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }
}
