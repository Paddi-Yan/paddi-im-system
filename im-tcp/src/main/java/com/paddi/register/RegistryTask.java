package com.paddi.register;

import com.paddi.config.BootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.paddi.common.constants.Constants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 22:43:58
 */
public class RegistryTask implements Runnable{

    private static Logger log = LoggerFactory.getLogger(RegistryTask.class);

    private ZookeeperManager zookeeperManager;

    private String ip;

    private BootstrapConfig.TcpConfig config;

    public RegistryTask(ZookeeperManager zookeeperManager, String ip, BootstrapConfig.TcpConfig config) {
        this.zookeeperManager = zookeeperManager;
        this.ip = ip;
        this.config = config;
    }

    @Override
    public void run() {
        zookeeperManager.createRootNode();
        String tcpRegistryPath = IM_CORE_ZKROOT + IM_CORE_ZKROOT_TCP + "/" + ip + ":" + config.getTcpPort();
        zookeeperManager.createChildNode(tcpRegistryPath);
        log.info("Register tcp server to zookeeper successfully, message = [{}]", tcpRegistryPath);

        String websocketRegistryPath = IM_CORE_ZKROOT + IM_CORE_ZKROOT_WEBSOCKET + "/" + ip + ":" + config.getWebSocketPort();
        zookeeperManager.createChildNode(websocketRegistryPath);
        log.info("Register tcp server to zookeeper successfully, message = [{}]", websocketRegistryPath);
    }
}
