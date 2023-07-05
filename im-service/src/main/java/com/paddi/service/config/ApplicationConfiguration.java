package com.paddi.service.config;

import com.paddi.common.enums.ConsistentHashAlgorithmEnum;
import com.paddi.common.enums.LoadBalanceStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:07:29
 */
@Data
@Component
@ConfigurationProperties(prefix = "im-server")
public class ApplicationConfiguration {
    private String zkServers = "127.0.0.1:2181";

    private Integer connectionTimeout = 3000;

    private Integer loadBalanceStrategy = LoadBalanceStrategyEnum.RANDOM.getCode();

    private Integer consistentHashAlgorithm = ConsistentHashAlgorithmEnum.TREE.getCode();

    private String callbackUrl;

    private boolean modifyUserAfterCallback; //用户资料变更之后回调开关

    private boolean addFriendAfterCallback; //添加好友之后回调开关

    private boolean addFriendBeforeCallback; //添加好友之前回调开关

    private boolean modifyFriendAfterCallback; //修改好友之后回调开关

    private boolean deleteFriendAfterCallback; //删除好友之后回调开关

    private boolean addFriendShipBlackAfterCallback; //添加黑名单之后回调开关

    private boolean deleteFriendShipBlackAfterCallback; //删除黑名单之后回调开关

    private boolean createGroupBeforeCallback; //创建群聊之前回调开关

    private boolean createGroupAfterCallback; //创建群聊之后回调开关

    private boolean modifyGroupAfterCallback; //修改群聊之后回调开关

    private boolean destroyGroupAfterCallback;//解散群聊之后回调开关

    private boolean deleteGroupMemberAfterCallback;//删除群成员之后回调

    private boolean addGroupMemberBeforeCallback;//拉人入群之前回调

    private boolean addGroupMemberAfterCallback;//拉人入群之后回调

    private boolean sendMessageAfterCallback;//发送单聊消息之后

    private boolean sendMessageBeforeCallback;//发送单聊消息之前

    private Integer deleteConversationSyncMode;

    private Integer offlineMessageCount;//离线消息最大条数
}
