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
}
