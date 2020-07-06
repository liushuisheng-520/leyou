package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录 查询用户信息加密 放入cookies
     *
     * @param username
     * @param password
     * @param response
     */
    public void login(String username, String password, HttpServletResponse response) {
        //1.查询用户信息 返回UserDTO
        UserDTO userDTO = userClient.queryUserByUsernameAndPassword(username, password);

        System.out.println(userDTO);

        //2.将UserDTO转换成UserInfo
        UserInfo userInfo = BeanHelper.copyProperties(userDTO, UserInfo.class);

        //3.将UserInfo加密
        //用配置类的方法获取私钥
        //用Jwt生成token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getUser().getExpire());

        //4.将获取token的放入到cookies中
        CookieUtils.newCookieBuilder().name(jwtProperties.getUser().getCookieName())
                .value(token)
                .domain(jwtProperties.getUser().getCookieDomain())
                .httpOnly(true) //禁用js操作cookie信息
                .response(response)
                .maxAge(60 * 60 * 24 * 7) //sookie的保存时间
                .build();


    }

    /**
     * 校验用户登录状态
     *
     * @param request
     * @return
     */
    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
        //有可能是假的token或者超时需要try catch一下

        try {
            //1.获取cookie
            String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());

            //2.解析token获取载荷
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);

            //判断paylocadId是否在redis的黑名单中 有就返回空
            if (stringRedisTemplate.hasKey(payload.getId())) {
                return null;
            }

            //获取超时时间
            Date expirationTime = payload.getExpiration();

            //3.获取UserInfo
            UserInfo userInfo = payload.getUserInfo();
            //如果当设定的超时时间小于15分钟 就重新生成一个token存入cookie中
            //超时时间-15分钟<= 当前时间 就创建token
            if (new DateTime(expirationTime).minusMinutes(jwtProperties.getUser().getMinRefreshInterval()).isBeforeNow()) {
                //jwtUtils加密
                token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getUser().getExpire());

                //重新生成一个新的token放入到cookie中
                CookieUtils.newCookieBuilder().name(jwtProperties.getUser().getCookieName())
                        .value(token)
                        .domain(jwtProperties.getUser().getCookieDomain())
                        .httpOnly(true) //禁用js操作cookie信息
                        .response(response)
                        .maxAge(60 * 60 * 24 * 7) //sookie的保存时间
                        .build();
            }


            return userInfo;

        } catch (Exception e) {
            return null; //如果token错误或者超时失效 就返回null
        }
    }

    /**
     * 用户注销登录
     *
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //1.获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        //获取载荷
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);

        //2.将token放入到redis
        //获取payloadId作为键
        String payloadId = payload.getId();
        //通过超时时间-当前时间 得到剩余时间
        Date expiration = payload.getExpiration();
        long l = expiration.getTime() - new Date().getTime();

        //用paylocadId作为键 剩余时间作为失效时间
        stringRedisTemplate.boundValueOps(payloadId).set("", l, TimeUnit.MILLISECONDS);

        //3.删除cookie
        CookieUtils.deleteCookie(jwtProperties.getUser().getCookieName(), jwtProperties.getUser().getCookieDomain(), response);


    }
}
