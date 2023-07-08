package com.paddi.service.config;

import com.paddi.common.enums.ConsistentHashAlgorithmEnum;
import com.paddi.common.enums.LoadBalanceStrategyEnum;
import com.paddi.common.loadbalance.LoadBalance;
import com.paddi.common.loadbalance.consistent.AbstractConsistentHashAlgorithm;
import com.paddi.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月04日 23:01:51
 */
@Configuration
public class BeanConfiguration {

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Bean
    public LoadBalance loadBalance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Integer loadBalanceStrategy = applicationConfiguration.getLoadBalanceStrategy();
        LoadBalanceStrategyEnum strategyEnum = LoadBalanceStrategyEnum.getStrategy(loadBalanceStrategy);
        LoadBalance loadBalance = (LoadBalance) Class.forName(strategyEnum.getClazz()).newInstance();
        if(strategyEnum == LoadBalanceStrategyEnum.HASH) {
            Method setStrategy = Class.forName(strategyEnum.getClazz())
                                      .getMethod("setAlgorithm", AbstractConsistentHashAlgorithm.class);
            Integer consistentHashAlgorithm = applicationConfiguration.getConsistentHashAlgorithm();
            ConsistentHashAlgorithmEnum algorithmEnum = ConsistentHashAlgorithmEnum.getAlgorithm(consistentHashAlgorithm);
            AbstractConsistentHashAlgorithm algorithm = (AbstractConsistentHashAlgorithm) Class.forName(algorithmEnum.getClazz()).newInstance();
            setStrategy.invoke(loadBalance, algorithm);
        }
        return loadBalance;
    }

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(applicationConfiguration.getZkServers(), applicationConfiguration.getConnectionTimeout());
    }

    @Bean
    public EasySQLInjector easySQLInjector() {
        return new EasySQLInjector();
    }

    @Bean
    public SnowflakeIdWorker buildSnowflakeSeq() throws Exception {
        return new SnowflakeIdWorker(0);
    }

}
