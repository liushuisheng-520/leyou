package com.leyou.order.controller;

import com.leyou.common.auth.entity.UserHolder;
import com.leyou.order.service.OrderService;
import com.leyou.order.vo.OrderVO;
import order.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 新增订单
     * @param orderDTO
     * @return
     */
    //http://api.leyou.com/api/order
    @PostMapping(value = "/order", name = "新增订单")
    public ResponseEntity<Long> addOrder(@RequestBody OrderDTO orderDTO) {

        String userId = UserHolder.getUserId();

       Long orderId= orderService.addOrder(userId,orderDTO);

        return ResponseEntity.ok(orderId);


    }

    /**
     * 根据订单Id查询订单及订单详情和物流详情
     * @param id
     * @return
     */
    //http://api.leyou.com/api/order/1234398310190682112
    @GetMapping(value = "/order/{id}",name = "根据订单Id查询订单及订单详情和物流详情")
    public ResponseEntity<OrderVO> findOrderById(@PathVariable Long id){

        String userId = UserHolder.getUserId();

        OrderVO orderVO= orderService.findOrderById(id,userId);

        return ResponseEntity.ok(orderVO);
    }

    /**
     * 获取微信支付连接CodeUrl
     * @param id
     * @return
     */
    //http:api.leyou.com/api/order/url/1234432470557003776
    @GetMapping(value = "/order/url/{id}",name = "获取微信支付连接CodeUrl")
    public ResponseEntity<String> getCodeUrl(@PathVariable Long id){



        String codeurl= orderService.getCodeUrl(id);

        return ResponseEntity.ok(codeurl);
    }


    /**
     * 查询支付状态
     * @param id
     * @return
     */
    //http://api.leyou.com/api/order/state/1234457968544714752
    @GetMapping(value = "/order/state/{id}",name = "查询支付状态")
    public ResponseEntity<Integer> queryOrderPayState(@PathVariable Long id){


        Integer payState= orderService.queryOrderPayState(id);

        return ResponseEntity.ok(payState);
    }



}
