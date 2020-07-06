package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.AuthProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * zuul网关过滤器(权限鉴定)
 */
@Component
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AuthProperties authProperties;


    @Override
    public String filterType() {//设置过滤类型 前置 进行中 后置 异常
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {//过滤的执行顺序  越小的越靠前执行
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() { //是否进入run方法

        //判断是否进入run方法
        List<String> allowPaths = authProperties.getAllowPaths();
        //需要获取请求地址uri
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI();//api.leyou.com/api/user/check/1235/1

        for (String allowPath : allowPaths) {
            //判断uri是否以allowPath中的内容开头
            if (uri.startsWith(allowPath)) {
                //是 就不用进入run方法中
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException { //执行run方法

        //获取request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();


        //1.获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());

        try {//如果解析异常就try catch
            //2.解析token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
            //获取userInfo
            UserInfo userInfo = payload.getUserInfo();
            //获取userId
            Long userId = userInfo.getId();
            ctx.addZuulRequestHeader("USER_ID", userId.toString());//网关向微服务发送请求 请求头中携带着userId


            //从userInfo中获取用户角色
            String role = userInfo.getRole();
            //普通用户 vip Svip等

            //todo  用户角色身份决定是否可以进入相关的微服务


        } catch (Exception e) {
            //停止解析
            ctx.setSendZuulResponse(false);
            //给个状态码403
            ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value());

            e.printStackTrace();

        }
        //


        return null;
    }
}
