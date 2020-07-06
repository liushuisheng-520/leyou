package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.entity.TbSku;

import java.util.Map;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbSkuService extends IService<TbSku> {


    void stockMinus(Map<Long, Integer> skuIdAndNumMap);

    void updateSkuStock(Map<Long, Integer> map);

}
