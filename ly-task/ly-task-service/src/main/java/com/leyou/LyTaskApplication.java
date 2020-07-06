package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 创建引导类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class LyTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyTaskApplication.class,args);
    }
}
