package com.leyou.cart.interceptor;

import com.leyou.common.auth.entity.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器(拦截网关的请求头 获取UserId)
 */
@Component
public class UserInterceptor implements HandlerInterceptor {
    @Autowired
    private HttpServletRequest request;


    //进入方法之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取userId
        String userId = request.getHeader("USER_ID");
        //将userId存入UserHoider实体类中
        UserHolder.setUserId(userId);

        return true;
    }

    //进入方法之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
