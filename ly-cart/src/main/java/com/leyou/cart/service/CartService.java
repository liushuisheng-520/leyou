package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static String prefix = "ly:cart:";

    /**
     * 将购物车商品存入redis中
     * @param userId
     * @param cart
     */
    public void addCartToRedis(String userId, Cart cart) {
        //获取skuId
        Long skuId = cart.getSkuId();


        //获取redis中所有关于userId的数据
        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(prefix + userId);
        //获取购物车中的商品
        String cartStr = hashOperations.get(skuId.toString());

        //判断redis中是否有商品 有 加数量
        if (StringUtils.isNotEmpty(cartStr)) {
            //将String转成对象
            Cart cart1 = JsonUtils.toBean(cartStr, Cart.class);
            //加数量
            cart.setNum(cart.getNum() + cart1.getNum());
        }

        //将数据存入redis中
        hashOperations.put(skuId.toString(),JsonUtils.toString(cart));


    }

    /**
     * 根据userId查询redis中的商品
     * @param userId
     * @return
     */
    public List<Cart> queryCartList(String userId) {
        //根据userId从redis中获取购物车
        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(prefix + userId);
        //获取redis购物车的Value值
        List<String> cartList = hashOperations.values();
        //判断结果是否有值
        if (CollectionUtils.isEmpty(cartList)){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }

        //因为获取的是String类型 所以要转成对象类型 并返回
        return cartList.stream().map(cartListJson->{
           return JsonUtils.toBean(cartListJson,Cart.class);
        }).collect(Collectors.toList());


    }

    /**
     * 修改购物城中的商品数量
     * @param userId
     * @param id
     * @param num
     */
    public void updataNum(String userId, Long id, Integer num) {

        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(prefix + userId);

        //获取购物车中的商品
        String cartStr = hashOperations.get(id.toString());
        //装换成Cart对象
        Cart cart = JsonUtils.toBean(cartStr, Cart.class);
        //修改商品数量
        cart.setNum(num);

        //重新放入到redis中
        hashOperations.put(id.toString(),JsonUtils.toString(cart));

    }

    /**
     * 根据skuId删除购物车商品
     * @param userId
     * @param id
     */
    public void deleteCart(String userId, Long id) {
        BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.boundHashOps(prefix + userId);

        hashOperations.delete(id.toString());
    }

    /**
     * 向redis中批量添加购物车
     * @param userId
     * @param cartList
     */
    public void addCartListToRedis(String userId, List<Cart> cartList) {

        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(prefix + userId);

        for (Cart cart : cartList) {
            Long skuId = cart.getSkuId();

            String cartStr = hashOperations.get(skuId.toString());
            //判断redis中是否有相同商品
            if (StringUtils.isNotEmpty(cartStr)){

                //如果有 将字符串转换成Cart对象
                Cart cart1 = JsonUtils.toBean(cartStr, Cart.class);
                //数量相加
                cart.setNum(cart.getNum()+cart1.getNum());

            }
            //如果没有 直接添加到redis中
            hashOperations.put(skuId.toString(),JsonUtils.toString(cart));


        }



    }
}
