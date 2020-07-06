package com.leyou.user.controller;

import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.entity.TbUserAddress;
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
public class UserAddressController {

    @Autowired
    private TbUserAddressService tbUserAddressService;

    /**
     * 根据UserAddessId查询地址信息
     * @param id
     * @return
     */
    //GET /address/byId?id=12
    @GetMapping("/address/byId")
    public ResponseEntity<UserAddressDTO> findUserAddressById(@RequestParam("id")Long id){

        TbUserAddress userAddress = tbUserAddressService.getById(id);

        UserAddressDTO userAddressDTO = BeanHelper.copyProperties(userAddress, UserAddressDTO.class);

        return ResponseEntity.ok(userAddressDTO);
    }
}
