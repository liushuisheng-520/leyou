package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.order.entity.TbOrder;
import com.leyou.order.mapper.TbOrderMapper;
import com.leyou.order.service.TbOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
@Service
public class TbOrderServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderService {

    @Override
    public void updateOvertimeOrder() {
        this.getBaseMapper().updateOvertimeOreder();


    }
}
