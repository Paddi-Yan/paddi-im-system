package com.paddi.register;

import org.I0Itec.zkclient.ZkClient;

import static com.paddi.common.constants.Constants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 22:31:58
 */
public class ZookeeperManager {

    private ZkClient zkClient;

    public ZookeeperManager(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     *  path: im-server/tcp/ip:port
     */
    public void createRootNode() {
        boolean exists = zkClient.exists(IM_CORE_ZKROOT);
        if(!exists) {
            zkClient.createPersistent(IM_CORE_ZKROOT);
        }

        boolean tpcExists = zkClient.exists(IM_CORE_ZKROOT + IM_CORE_ZKROOT_TCP);
        if(!tpcExists) {
            zkClient.createPersistent(IM_CORE_ZKROOT + IM_CORE_ZKROOT_TCP, true);
        }
        boolean webExists = zkClient.exists(IM_CORE_ZKROOT + IM_CORE_ZKROOT_WEBSOCKET);
        if(!webExists) {
            zkClient.createPersistent(IM_CORE_ZKROOT + IM_CORE_ZKROOT_WEBSOCKET, true);
        }
    }

    public void createChildNode(String path) {
        if(!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }
    }
}