package com.leyou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.order.entity.TbOrder;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
public interface TbOrderService extends IService<TbOrder> {

    void updateOvertimeOrder();

}
