package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 创建引导类
 */
@SpringBootApplication
@EnableConfigServer //配置中心注解
public class LyConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyConfigApplication.class,args);
    }
}
