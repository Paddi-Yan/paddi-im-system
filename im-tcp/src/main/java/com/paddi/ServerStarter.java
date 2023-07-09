package com.paddi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.paddi.config.BootstrapConfig;
import com.paddi.factory.RocketMQFactory;
import com.paddi.listener.MessageListener;
import com.paddi.listener.UserLoginMessageListener;
import com.paddi.redis.RedisManager;
import com.paddi.register.RegistryTask;
import com.paddi.register.ZookeeperManager;
import com.paddi.server.NettyServer;
import com.paddi.server.NettyWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月03日 10:39:20
 */
@Slf4j
public class ServerStarter {

    public static void main(String[] args){
        if(args.length > 0) {
            start(args[0]);
        }
    }

    private static void start(String path) {
        try {
            BootstrapConfig bootstrapConfig = initConfig(path);
            //部分初始化存在依赖关系-需要按照顺序进行初始化
            RedisManager.init(bootstrapConfig.getServer().getRedis());
            new NettyServer(bootstrapConfig.getServer()).start();
            new NettyWebSocketServer(bootstrapConfig.getServer()).start();
            startMessageListener(bootstrapConfig);
            RocketMQFactory.init(bootstrapConfig.getServer().getRocketmq());
            MessageListener.init(bootstrapConfig.getServer());
            registerToZookeeper(bootstrapConfig.getServer());
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

    private static void startMessageListener(BootstrapConfig bootstrapConfig) {
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(bootstrapConfig.getServer().getLoginModel());
        userLoginMessageListener.listenUserLogin();
    }

    private static void registerToZookeeper(BootstrapConfig.TcpConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getZookeeper().getAddress(), config.getZookeeper().getConnectTimeout());
        ZookeeperManager zookeeperManager = new ZookeeperManager(zkClient);
        //TODO 使用线程池
        RegistryTask registryTask = new RegistryTask(zookeeperManager, hostAddress, config);
        Thread thread = new Thread(registryTask);
        thread.start();
    }

    private static BootstrapConfig initConfig(String path) {
        Yaml yaml = new Yaml();
        BootstrapConfig bootstrapConfig = yaml.loadAs(ResourceUtil.getReader(path, StandardCharsets.UTF_8), BootstrapConfig.class);
        log.info("加载配置:{}", bootstrapConfig);
        return bootstrapConfig;
    }
}
