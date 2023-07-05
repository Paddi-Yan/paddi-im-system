package com.paddi.service.manager;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.paddi.common.constants.Constants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:12:45
 */
@Component
@Slf4j
public class ZookeeperManager {

    @Autowired
    private ZkClient zkClient;

    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(IM_CORE_ZKROOT + IM_CORE_ZKROOT_TCP);
        log.info("query all tcp server node = [{}] successfully", JSON.toJSONString(children));
        return children;
    }

    public List<String> getAllWebSocketNode() {
        List<String> children = zkClient.getChildren(IM_CORE_ZKROOT + IM_CORE_ZKROOT_WEBSOCKET);
        log.info("query all websocket server node = [{}] successfully", JSON.toJSONString(children));
        return children;
    }

}
