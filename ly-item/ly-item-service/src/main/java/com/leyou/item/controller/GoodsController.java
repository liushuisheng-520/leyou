package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import com.leyou.item.service.TbSpuService;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping(value = "/spu/page", name = "分页查询spu数据")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable
    ) {
        PageResult<SpuDTO> pageResult = goodsService.findSpuByPage(page, rows, key, saleable);
        return ResponseEntity.ok(pageResult);

    }

    @PostMapping(value = "/goods", name = "保存商品信息")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 保存修改后的商品信息
     *
     * @param spuDTO
     * @return
     */
    @PutMapping(value = "/goods", name = "保存修改后的商品信息")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 商品的上下架(spu,sku)
     *
     * @param id
     * @param saleable
     * @return
     */
    @PutMapping(value = "/spu/saleable", name = "商品的上下架")
    public ResponseEntity<Void> updateSpuBySpuId(@RequestParam("id") Long id,
                                                 @RequestParam("saleable") Boolean saleable) {

        goodsService.updateSpuBySpuId(id, saleable);

        return ResponseEntity.ok().build();
    }

    /**
     * (修改回显)根据spuId查询spudetail的信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/spu/detail", name = "根据spuId查询spudetail的信息")
    public ResponseEntity<SpuDetailDTO> findSPUDetailBySpuId(@RequestParam("id") Long id) {

        SpuDetailDTO spuDetailDTO = goodsService.findSPUDetailBySpuId(id);

        return ResponseEntity.ok(spuDetailDTO);
    }

    /**
     * (回显)根据spuId查询sku的数据
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/sku/of/spu", name = "根据spuId查询sku的数据")
    public ResponseEntity<List<SkuDTO>> findSKUBySpuId(@RequestParam("id") Long id) {
        List<SkuDTO> skuDTOList = goodsService.findSKUBySpuId(id);

        return ResponseEntity.ok(skuDTOList);
    }

    /**
     * 根据spuId查询Spu信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuBySpuId(@PathVariable("id") Long id) {

        SpuDTO spuDTO = goodsService.findSpuBySpuId(id);

        return ResponseEntity.ok(spuDTO);

    }

    //http://api.leyou.com/api/item/sku/list?ids=27359021584

    /**
     * 未登录购物车时 根据skuIds查询sku集合
     *
     * @param ids
     * @return
     */
    @GetMapping(value = "/sku/list", name = "根据skuIds查询sku集合")
    public ResponseEntity<List<SkuDTO>> findSkuBySkuIds(@RequestParam("ids") List<Long> ids) {

        List<SkuDTO> skuDTOList = goodsService.findSkuBySkuIds(ids);

        return ResponseEntity.ok(skuDTOList);
    }

    /**
     * 减库存
     *
     * @param
     * @return
     */
    //PUT /stock/minus
    @PutMapping(value = "/stock/minus", name = "减库存")
    public ResponseEntity<Void> stockMinus(@RequestBody Map<Long, Integer> skuIdAndNumMap) {

        goodsService.stockMinus(skuIdAndNumMap);

        return ResponseEntity.ok().build();
    }

    /**
     * 恢复增加库存
     * @param map
     * @return
     */
    //PUT /stock/plus
    @PutMapping(value = "/stock/plus",name = "恢复增加库存")
    public ResponseEntity<Void> updateOvertimeOrderBySkuSum(@RequestBody Map<Long, Integer> map) {
        goodsService.updateOvertimeOrderBySkuSum(map);

        return ResponseEntity.ok().build();
    }
}
