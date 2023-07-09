package com.paddi.config;

import lombok.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 11:07:37
 */
@Data
@ToString
public class BootstrapConfig {

    private TcpConfig server;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TcpConfig {

        /**
         * TCP绑定端口号
         */
        private Integer tcpPort;
        /**
         * WebSocket绑定端口号
         */
        private Integer webSocketPort;
        /**
         * 是否开启WebSocket
         */
        private boolean enableWebSocket;
        /**
         * boss线程
         */
        private Integer bossThreadSize;
        /**
         * worker线程
         */
        private Integer workerThreadSize;

        /**
         * 心跳超时时间 单位毫秒
         */
        private Long heartBeatTime;

        /**
         * 多端登陆模式
         */
        private Integer loginModel;


        private RedisConfig redis;

        private RocketMQConfig rocketmq;

        private ZookeeperConfig zookeeper;

        private Integer brokerId;

        private String logicUrl;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RedisConfig {
        /**
         * 单机模式：single 哨兵模式：sentinel 集群模式：cluster
         */
        private String mode;
        /**
         * 数据库
         */
        private Integer database;
        /**
         * 密码
         */
        private String password;
        /**
         * 超时时间
         */
        private Integer timeout;
        /**
         * 最小空闲数
         */
        private Integer poolMinIdle;
        /**
         * 连接超时时间(毫秒)
         */
        private Integer poolConnTimeout;
        /**
         * 连接池大小
         */
        private Integer poolSize;

        /**
         * redis单机配置
         */
        private RedisSingle single;


    }

    /**
     * Redis单机配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSingle {
        /**
         * 地址
         */
        private String address;
    }

    /**
     * RocketMQ配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RocketMQConfig {
        private String namesrvAddr;

        private String producerGroup;
    }

    @Data
    public static class ZookeeperConfig {
        /**
         * zk连接地址
         */
        private String address;

        /**
         * zk连接超时时间
         */
        private Integer connectTimeout;
    }

}
