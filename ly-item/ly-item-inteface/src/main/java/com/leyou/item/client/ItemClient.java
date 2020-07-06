package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * feign接口 用于连接ly-search
 */
@FeignClient("item-service")
public interface ItemClient {

    /**
     * 根据分类id查询品牌数据
     * @param id
     * @return
     */
    @GetMapping(value = "/brand/of/category",name = "根据分类id查询品牌数据")
    public List<BrandDTO> findBrandListByCategoryId(@RequestParam("id") Long id);

    /**
     * 分页查询spu数据
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping(value = "/spu/page", name = "分页查询spu数据")
    public PageResult<SpuDTO> findSpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable
    );


    /**
     * (修改回显)根据spuId查询spudetail的信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/spu/detail", name = "根据spuId查询spudetail的信息")
    public SpuDetailDTO findSPUDetailBySpuId(@RequestParam("id") Long id);


    /**
     * (回显)根据spuId查询sku的数据
     * @param id
     * @return
     */
    @GetMapping(value = "/sku/of/spu",name = "根据spuId查询sku的数据")
    public List<SkuDTO> findSKUBySpuId(@RequestParam("id")Long id);

    /**
     * 根据分类id或组id查询规格参数数据
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping(value = "/spec/params",name ="根据分类id或组id查询规格参数数据" )
//    gid  cid  searching
    public List<SpecParamDTO> findSpecParamByCategoryIdOrGroupId(
            @RequestParam(value = "gid" ,required = false) Long gid,
            @RequestParam(value ="cid" ,required = false) Long cid,
            @RequestParam(value ="searching",required = false) Boolean searching
    );


    /**
     * 根据品牌Id集合查询品牌信息集合
     * @param ids
     * @return
     */
    @GetMapping(value = "/brand/list",name = "根据品牌Id查询品牌信息")
    public List<BrandDTO> findBrandByIds(@RequestParam("ids") List<Long> ids);


    /**
     * 根据分类Id集合查询分类数据集合
     * @param ids
     * @return
     */
    @GetMapping(value = "/category/list",name = "根据分类Id集合查询分类数据集合")
    public List<CategoryDTO> findCategoryByIds(@RequestParam("ids")List<Long> ids);


    /**
     * 根据spuId查询Spu信息
     * @param id
     * @return
     */
    @GetMapping(value = "/spu/{id}")
    public SpuDTO findSpuBySpuId(@PathVariable("id")Long id);


    /**
     * 根据品牌Id查询品牌信息
     * @param
     * @return
     */
    @GetMapping(value = "/brand/{id}",name = "根据品牌Id查询品牌信息")
    public BrandDTO findBrandById(@PathVariable("id") Long id);


    /**
     *根据categoryId查询规格参数组和组内参数
     * @param id
     * @return
     */
    @GetMapping(value = "/spec/of/category",name ="根据categoryId查询规格参数组和组内参数" )
    public List<SpecGroupDTO> findSpecGroupWithParamsByCategoryId(@RequestParam(value = "id")Long id);


    /**
     * 根据skuIds查询sku集合
     * @param ids
     * @return
     */
    @GetMapping(value = "/sku/list",name = "根据skuIds查询sku集合")
    public List<SkuDTO> findSkuBySkuIds(@RequestParam("ids") List<Long> ids);


    /**
     * 减库存
     * @param
     * @return
     */
    //PUT /stock/minus
    @PutMapping(value = "/stock/minus",name = "减库存")
    public Void stockMinus(@RequestBody Map<Long,Integer> skuIdAndNumMap);

    /**
     * 恢复增加库存
     * @param map
     * @return
     */
    //PUT /stock/plus
    @PutMapping(value = "/stock/plus",name = "恢复增加库存")
    public Void updateOvertimeOrderBySkuSum(@RequestBody Map<Long, Integer> map);
}
