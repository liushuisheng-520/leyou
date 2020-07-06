package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    /**
     *用户登录 查询用户信息加密 放入cookies
     * @param username
     * @param password
     * @return
     */
    //POST /login
    @PostMapping(value = "/login", name = "用户登录 查询用户信息加密 放入cookies")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        authService.login(username,password,response);

        return ResponseEntity.ok().build();

    }

    /**
     * 校验用户登录状态
     * @return
     */
    //GET/verify
    @GetMapping(value = "/verify",name = "校验用户登录状态")
    public ResponseEntity<UserInfo> verify(){
        UserInfo userInfo= authService.verify(request,response);

        return ResponseEntity.ok(userInfo);

    }

    /**
     * 用户注销
     * @return
     */
    //http://api.leyou.com/api/auth/logout
    @PostMapping (value = "/logout",name = "用户注销")
    public ResponseEntity<Void> logout(){
        authService.logout(request,response);

        return ResponseEntity.ok().build();

    }
}
