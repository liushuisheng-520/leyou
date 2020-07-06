package com.leyou.user.controller;

import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.service.TbUserAddressService;
import com.leyou.user.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private TbUserService tbUserService;
    @Autowired
    private TbUserAddressService tbUserAddressService;

    //GET /check/{data}/{type}

    /**
     * 校验用户名和手机号是否唯一
     * @param data
     * @param type
     * @return
     */
    @GetMapping(value = "/check/{data}/{type}",name = "校验用户名和手机号是否唯一")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) {

       Boolean b= tbUserService.checkData(data,type);
        //返回结果为Boolean类型 状态码是200,400,500
        return ResponseEntity.ok(b);
    }

    /**
     * 发送短信
     * @param phone
     * @return
     */
    //http://api.leyou.com/api/user/code
    @PostMapping (value = "/code",name = "发送短信")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone) {

        tbUserService.sendCode(phone);
        //返回 状态码是204,400,500
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册和矫正验证码和密码加密(hibernate validator后台验证用户信息)
     * @param
     * @return
     */
    @PostMapping (value = "/register",name = "注册和矫正验证码和密码加密")
    public ResponseEntity<Void> register(@Valid TbUser user, BindingResult result, @RequestParam("code")String code) {
        //如果用hibernate validator验证不通过时 应该把错误原因抛出去
        //判断是否有错 如有错就获取到这个错误
        if (result.hasErrors()){
            List<FieldError> fieldErrors = result.getFieldErrors();
            String errorMassage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("|"));

        throw new LyException(500,errorMassage);
        }


        tbUserService.register(user,code);
        //返回 状态码是204,400,500
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
//GET /query
    @GetMapping (value = "/query",name = "根据用户名和密码查询用户")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(@RequestParam("username")String username,
                                                                  @RequestParam("password")String password) {

       UserDTO userDTO= tbUserService.queryUserByUsernameAndPassword(username,password);
        //返回 状态码是204,400,500
        return ResponseEntity.ok(userDTO);
    }



}
