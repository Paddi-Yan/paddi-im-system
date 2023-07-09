package com.paddi.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月09日 16:27:35
 */
@SpringBootApplication
@MapperScan("com.paddi.message.mapper")
public class MessageStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageStoreApplication.class, args);
    }
}
