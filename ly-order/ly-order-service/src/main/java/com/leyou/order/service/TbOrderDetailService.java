package com.leyou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.order.entity.TbOrderDetail;

import java.util.List;


/**
 * <p>
 * 订单详情表 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
public interface TbOrderDetailService extends IService<TbOrderDetail> {

    List<TbOrderDetail> findOvertimeOrderDetail();

}
