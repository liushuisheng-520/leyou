package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 创建引导类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LyCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyCartApplication.class,args);
    }
}
