package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.order.entity.TbOrderDetail;
import com.leyou.order.mapper.TbOrderDetailMapper;
import com.leyou.order.service.TbOrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
@Service
public class TbOrderDetailServiceImpl extends ServiceImpl<TbOrderDetailMapper, TbOrderDetail> implements TbOrderDetailService {
    /**
     * 获取超时订单中的商品和数量
     * @return
     */
    @Override
    public List<TbOrderDetail> findOvertimeOrderDetail() {


        return this.getBaseMapper().findOvertimeOrderDetail();
    }
}
