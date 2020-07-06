package com.leyou.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.config.PayProperties;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.entity.TbOrderDetail;
import com.leyou.order.entity.TbOrderLogistics;
import com.leyou.order.enums.BusinessTypeEnum;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.vo.OrderDetailVO;
import com.leyou.order.vo.OrderLogisticsVO;
import com.leyou.order.vo.OrderVO;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserAddressDTO;
import order.dto.CartDTO;
import order.dto.OrderDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional//事务管理
public class OrderService {
    //操作三张表
    @Autowired
    private TbOrderService tbOrderService;
    @Autowired
    private TbOrderDetailService tbOrderDetailService;
    @Autowired
    private TbOrderLogisticsService tbOrderLogisticsService;
    @Autowired
    private IdWorker idWorker;//雪花算法生成orderId
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private UserClient userClient;


    public Long addOrder(String userId, OrderDTO orderDTO) {


        //雪花算法生成orderId
        long orderId = idWorker.nextId();//分布式Id生成算法

        //1.保存订单表
        TbOrder tbOrder = new TbOrder();

        //保存orderId
        tbOrder.setOrderId(orderId);

        //保存总金额
        List<CartDTO> carts = orderDTO.getCarts();
        //转换成Map
        Map<Long, Integer> sukAndNumMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));

        //获取skuId集合
        List<Long> skuIdList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //通过skuId获取sku对象
        List<SkuDTO> skuList = itemClient.findSkuBySkuIds(skuIdList);

        //最后将订单详情对象保存到list集合中
        ArrayList<TbOrderDetail> orderDetailList = new ArrayList<>();

        Long totalFee = 0L;
        for (SkuDTO sku : skuList) {

            Integer num = 0;
            //根据skuId获取num
            num = sukAndNumMap.get(sku.getId());
            //计算总金额
            totalFee += sku.getPrice() * num;


            //2.保存订单详情表
            TbOrderDetail tbOrderDetail = BeanHelper.copyProperties(sku, TbOrderDetail.class);

            //保存订单Id
            tbOrderDetail.setOrderId(orderId);

            //保存skuId
            tbOrderDetail.setSkuId(sku.getId());

            //保存购买数量
            tbOrderDetail.setNum(num);

            //保存图片
            tbOrderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            //最后把订单详情放入到集合中  减少msq交互 方便保存
            orderDetailList.add(tbOrderDetail);


        }
        tbOrder.setTotalFee(totalFee);

        //保存实付金额
        tbOrder.setActualFee(tbOrder.getTotalFee());//单位是 分

        //保存支付类型
        tbOrder.setPaymentType(orderDTO.getPaymentType());//单位是 分

        //保存用户Id
        tbOrder.setUserId(Long.parseLong(userId));

        //保存订单状态
        tbOrder.setStatus(OrderStatusEnum.INIT.value());//用枚举类
        //保存支付时间

        //保存业务类型
        tbOrder.setBType(BusinessTypeEnum.MALL.value());//用枚举类

        //保存邮费
        tbOrder.setPostFee(0l);


        //3.保存订单物流表
        //通过接口获取地址
        UserAddressDTO addressById = userClient.findUserAddressById(orderDTO.getAddressId());
        //因为两者有相同 所以用BeanHelper转换一下
        TbOrderLogistics tbOrderLogistics = BeanHelper.copyProperties(addressById, TbOrderLogistics.class);
        //保存orderId
        tbOrderLogistics.setOrderId(orderId);


        //在这里统一保存三张表的信息
        //执行保存订单
        boolean save = tbOrderService.save(tbOrder);
        //判断是否保存成功
        if (!save) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //执行批量保存订单详情
        save = tbOrderDetailService.saveBatch(orderDetailList);
        if (!save) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //执行保存物流信息
        save = tbOrderLogisticsService.save(tbOrderLogistics);
        if (!save) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }


        //调用接口减库存 修改sku表中的stock库存数量
        itemClient.stockMinus(sukAndNumMap);

        return orderId;
    }

    /**
     * 根据订单Id查询订单及订单详情和物流详情
     *
     * @param id
     * @return
     */
    public OrderVO findOrderById(Long id, String userId) {
        //这种方法不安全 容易通过订单号查询到订单信息
        //TbOrder order = tbOrderService.getById(id);//getById==getByPrimaryKey 不要忘了实体类中order属性上添加主键注释@TableId

        //查询订单信息(推荐使用这种方法  安全)
        QueryWrapper<TbOrder> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(TbOrder::getUserId, Long.parseLong(userId));
        queryWrapper1.lambda().eq(TbOrder::getOrderId, id);
        queryWrapper1.lambda().eq(TbOrder::getStatus, OrderStatusEnum.INIT.value());
        TbOrder order = tbOrderService.getOne(queryWrapper1);
        //判断结果是否为空
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }


        OrderVO orderVO = BeanHelper.copyProperties(order, OrderVO.class);

        //查询订单详情 和订单是一对多的关系
        QueryWrapper<TbOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbOrderDetail::getOrderId, order.getOrderId());
        List<TbOrderDetail> orderDetails = tbOrderDetailService.list(queryWrapper);
        List<OrderDetailVO> orderDetailVOS = BeanHelper.copyWithCollection(orderDetails, OrderDetailVO.class);
        orderVO.setDetailList(orderDetailVOS);

        //查询物流信息 和订单是一对一的关系 不要忘了实体类中order属性上添加主键注释@TableId
        TbOrderLogistics logistics = tbOrderLogisticsService.getById(id);
        OrderLogisticsVO orderLogisticsVO = BeanHelper.copyProperties(logistics, OrderLogisticsVO.class);
        orderVO.setLogistics(orderLogisticsVO);


        return orderVO;
    }

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PayProperties payProperties;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取微信支付连接CodeUrl
     *
     * @param orderId
     * @return
     */
    private String prefix = "ly:order:pay";

    public String getCodeUrl(Long orderId) {

        String code_url = redisTemplate.boundValueOps(prefix + orderId).get();
        //判断结果是否为空
        if (StringUtils.isNotBlank(code_url)) {
            //有就直接返回
            return code_url;
        }

        //没有 重新获取
        try {

            //获取订单实付价格
            TbOrder order = tbOrderService.getById(orderId);

            //同一下单的地址 获取Code url
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";


            Map<String, String> map = new HashMap<>();

            map.put("appid", payProperties.getAppID()); //公众号id
            map.put("mch_id", payProperties.getMchID()); //商户号id
            map.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            map.put("body", "乐优商城支付"); //商品描述
            map.put("out_trade_no", orderId.toString()); //商户订单号
            map.put("total_fee", order.getActualFee().toString()); //标价金额
            map.put("spbill_create_ip", "127.0.0.1"); //终端IP
            map.put("notify_url", payProperties.getNotifyurl()); //通知地址
            map.put("trade_type", "NATIVE"); //交易类型

            //获取签名并将map转换成XML格式
            String paramXml = WXPayUtil.generateSignedXml(map, payProperties.getKey());
            //用restTemplate远程调用统一下单API地址
            String codeUrl = restTemplate.postForObject(url, paramXml, String.class);
            //将获取的XML结果转换成Map
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(codeUrl);
            //获取到code_Url
            code_url = xmlToMap.get("code_url");

            //优化 将code_url存到redis中 时效2小时
            redisTemplate.boundValueOps(prefix + orderId).set(code_url, 2, TimeUnit.HOURS);

            return code_url;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询支付状态
     *
     * @param orderId
     * @return
     */
    public Integer queryOrderPayState(Long orderId) {

        try {
            //同一下单的地址 获取Code url
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";


            Map<String, String> map = new HashMap<>();

            map.put("appid", payProperties.getAppID()); //公众号id
            map.put("mch_id", payProperties.getMchID()); //商户号id
            map.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            map.put("out_trade_no", orderId.toString()); //商户订单号

            //获取签名并将map转换成XML格式
            String paramXml = WXPayUtil.generateSignedXml(map, payProperties.getKey());
            //用restTemplate远程调用统一下单API地址
            String codeUrl = restTemplate.postForObject(url, paramXml, String.class);
            //将获取的XML结果转换成Map
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(codeUrl);
            //获取到code_Url
            String paystate = xmlToMap.get("trade_state");
            //判断结果是否为SUCCESS成功
            if ("SUCCESS".equals(paystate)) {
                //支付成功修改订单状态和添加支付时间
                TbOrder order = new TbOrder();
                order.setOrderId(orderId);
                order.setStatus(OrderStatusEnum.PAY_UP.value());
                order.setPayTime(new Date());
                //根据orderId修改
                tbOrderService.updateById(order);


                return 1;
            } else {
                return 0;
            }

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 定时清理订单 恢复库存数量
     */
    @Transactional //事务
    public void cleanOvertimeOrder() {

        //1.获取超时订单中的商品和数量
//        SELECT od.`sku_id`,SUM(od.num) FROM tb_order o,tb_order_detail od WHERE o.`order_id`=od.`order_id`
//
//        AND o.`status`=1 AND TIMESTAMPDIFF(MINUTE,O.`create_time`,NOW())>60 GROUP BY od.`sku_id`
        List<TbOrderDetail> orderDetails = tbOrderDetailService.findOvertimeOrderDetail();
        System.out.println(orderDetails);


        //2.修改超时订单的状态
        //update tb_order O set status=5 where status=1 and TIMESTAMPDIFF(MINUTE,create_time,NOW())>60
        tbOrderService.updateOvertimeOrder();


        //3.恢复商品库存数量
        //update tb_sku set stock=stock+sum where sku_id=?
        //转换成Map
        Map<Long, Integer> map = orderDetails.stream().collect(Collectors.toMap(TbOrderDetail::getSkuId, TbOrderDetail::getNum));

        itemClient.updateOvertimeOrderBySkuSum(map);


    }
}
