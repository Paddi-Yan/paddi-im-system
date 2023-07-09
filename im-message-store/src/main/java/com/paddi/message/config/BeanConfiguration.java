package com.paddi.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:22:49
 */
@Configuration
public class BeanConfiguration {
    @Bean
    public EasySQLInjector easySQLInjector() {
        return new EasySQLInjector();
    }
}
