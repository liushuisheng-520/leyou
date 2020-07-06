package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 创建引导类
 */
/*@SpringBootApplication
@EnableDiscoveryClient //把当前服务注册到注册中心 可省略
@EnableCircuitBreaker  //熔断开启*/
@SpringCloudApplication  //其中包含了SpringBootApplication,EnableDiscoveryClient,@EnableCircuitBreaker
@EnableZuulProxy  //网关注解
public class LyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyGatewayApplication.class,args);
    }
}
