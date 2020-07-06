package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.item.entity.TbSku;
import com.leyou.item.mapper.TbSkuMapper;
import com.leyou.item.service.TbSkuService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSkuServiceImpl extends ServiceImpl<TbSkuMapper, TbSku> implements TbSkuService {
    /**
     * 减库存
     *
     * @param skuIdAndNumMap
     */
    @Override
    public void stockMinus(Map<Long, Integer> skuIdAndNumMap) {

        for (Long skuId : skuIdAndNumMap.keySet()) {

            Integer num = skuIdAndNumMap.get(skuId);

            this.getBaseMapper().stockMinus(skuId, num);

        }
    }

    /**
     * 恢复库存
     *
     * @param map
     */
    @Override
    public void updateSkuStock(Map<Long, Integer> map) {
        for (Long skuId : map.keySet()) {
            Integer num = map.get(skuId);
            this.getBaseMapper().updateSkuStock(skuId, num);

        }


    }
}
