package com.paddi.common.constants;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 13:48:51
 */
public class Constants {

    /** channel绑定的userId Key*/
    public static final String USERID = "userId";

    /** channel绑定的appId */
    public static final String APPID = "appId";

    public static final String CLIENT_TYPE = "clientType";

    public static final String COMMAND = "command";
    public static final String IMEI = "imei";

    /** channel绑定的clientType 和 imei Key*/
    public static final String CLIENT_IMEI = "clientImei";

    public static final String READ_TIME = "readTime";

    public static final String IM_CORE_ZKROOT = "/im-server";

    public static final String IM_CORE_ZKROOT_TCP = "/tcp";

    public static final String IM_CORE_ZKROOT_WEBSOCKET = "/websocket";

    public static class UserSignConstants{
        public static final String TLS_APPID = "TLS.appId";
        public static final String TLS_IDENTIFIER = "TLS.identifier";
        public static final String TLS_EXPIRE = "TLS.expire";
        public static final String TLS_SIGN_TIME = "TLS.signTime";
    }

    public static class RedisConstants{

        /**
         * userSign，格式：appId:userSign:identifier:userSign
         */
        public static final String USER_SIGN = ":user-sign:";

        /**
         * 用户上线通知channel
         */
        public static final String USER_LOGIN_CHANNEL = "signal/channel/LOGIN_USER_INNER_QUEUE";


        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000：userSession：lld
         */
        public static final String USER_SESSION = ":user-session:";

        /**
         * 缓存客户端消息防重，格式： appId + :cacheMessage: + messageId
         */
        public static final String CACHE_MESSAGE = "cache-message";

        public static final String OFFLINE_MESSAGE = "offline-message";

        /**
         * seq 前缀
         */
        public static final String SEQ_PREFIX = "seq";

        /**
         * 用户订阅列表，格式 ：appId + :subscribe: + userId。Hash结构，filed为订阅自己的人
         */
        public static final String SUBSCRIBE = "subscribe";

        /**
         * 用户自定义在线状态，格式 ：appId + :userCustomerStatus: + userId。set，value为用户id
         */
        public static final String USER_CUSTOMER_STATUS = "user-customer-status";

    }

    public static class RocketMQConstants {

        public static final String Im2UserService = "pipeline2UserService";

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String GroupService2Im = "GroupService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";

        public static final String MESSAGE_SERVICE_GROUP = "ChatMessage";

    }

    public static class CallbackCommand{
        public static final String ModifyUserAfter = "user.modify.after";

        public static final String CreateGroupBefore = "group.create.before";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DestroyGroupAfter = "group.destroy.after";

        public static final String TransferGroupAfter = "group.transfer.after";

        public static final String GroupMemberAddBefore = "group.member.add.before";

        public static final String GroupMemberAddAfter = "group.member.add.after";

        public static final String GroupMemberDeleteAfter = "group.member.delete.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendBefore = "friend.update.before";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlack = "black.delete";

        public static final String SendMessageAfter = "message.send.after";

        public static final String SendMessageBefore = "message.send.before";

    }

    public static class SeqConstants {
        public static final String Message = "messageSeq";

        public static final String GroupMessage = "groupMessageSeq";


        public static final String Friendship = "friendshipSeq";

        //        public static final String FriendshipBlack = "friendshipBlackSeq";

        public static final String FriendshipRequest = "friendshipRequestSeq";

        public static final String FriendshipGroup = "friendshipGroupSeq";

        public static final String Group = "groupSeq";

        public static final String Conversation = "conversationSeq";

    }

}
