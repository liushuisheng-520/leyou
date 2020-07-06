package com.leyou.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 白名单配置类
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ly.filter")
public class AuthProperties {

    //filter:
    //    allowPaths:
    //      - /api/auth/login
    //      - /api/search
    //      - /api/user/register
    //      - /api/user/check
    //      - /api/user/code
    //      - /api/item
    //      - /zuul/api/upload

    //写成集合
    private List<String> allowPaths;

}
