package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.common.auth.entity.UserHolder;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartCotroller {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车 将商品存入到redis中
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {

        //获取用户userId
        String userId = UserHolder.getUserId();

        cartService.addCartToRedis(userId,cart);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 根据userId查询redis中的商品
     * @return
     */
    @GetMapping(value = "/list",name = "根据userId查询redis中的商品")
    public ResponseEntity<List<Cart>> queryCartList() {

        //获取用户userId
        String userId = UserHolder.getUserId();

        List<Cart> cartList= cartService.queryCartList(userId);

        return ResponseEntity.ok(cartList);

    }

    /**
     * 修改购物城中的商品数量
     * @param id
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updataNum(@RequestParam("id")Long id,@RequestParam("num")Integer num) {

        //获取用户userId
        String userId = UserHolder.getUserId();

        cartService.updataNum(userId,id,num);

        return ResponseEntity.ok().build();

    }

    /**
     * 根据skuId删除购物车商品
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}",name = "根据skuId删除购物车商品")
    public ResponseEntity<Void> deleteCart(@PathVariable("id")Long id) {

        //获取用户userId
        String userId = UserHolder.getUserId();

        cartService.deleteCart(userId,id);

        return ResponseEntity.ok().build();

    }

    /**
     * 向redis中批量添加购物车
     * @param cartList
     * @return
     */
    //POST /list
    @PostMapping(value = "/list",name = "登录后合并购物车,向redis中批量添加购物车")
    public ResponseEntity<Void> addCartListToRedis(@RequestBody List<Cart> cartList) {

        //获取用户userId
        String userId = UserHolder.getUserId();

        cartService.addCartListToRedis(userId,cartList);
        //无返回结果 返回200状态码
        return ResponseEntity.ok().build();

    }


}
