package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 配置类
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private String priKeyPath;


    private PublicKey publicKey; //根据公钥文件路径生成公钥
    private PrivateKey privateKey; //根据私钥文件路径生成私钥

    //必须pubKeyPath和priKeyPath有值才能执行该方法(生成公钥和私钥的方法)
    @Override
    public void afterPropertiesSet() throws Exception {

        publicKey = RsaUtils.getPublicKey(pubKeyPath);
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }


    private UserTokenProperties user = new UserTokenProperties();

    @Data
    public class UserTokenProperties {
        /**
         * token过期时长
         */
        private int expire;
        /**
         * 存放token的cookie名称
         */
        private String cookieName;
        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;

        private Integer MinRefreshInterval;
    }

}