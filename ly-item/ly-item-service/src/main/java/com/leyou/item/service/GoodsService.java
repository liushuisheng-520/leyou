package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.omg.CORBA.IRObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Service
public class GoodsService {
//    注入spuService skuService

    @Autowired
    private TbSpuService spuService;
    @Autowired
    private TbSpuDetailService spuDetailService;
    @Autowired
    private TbSkuService skuService;
    @Autowired
    private TbBrandService brandService;
    @Autowired
    private TbCategoryService categoryService;


public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
    QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
    Page<TbSpu> page1 = new Page<>(page,rows);  //构建了分页

//        关键字查询
    if(StringUtils.isNotBlank(key)){
        queryWrapper.lambda().like(TbSpu::getName,key);
    }
    if(saleable!=null){
        queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
    }
    IPage<TbSpu> spuIPage = spuService.page(page1, queryWrapper);
    long total = spuIPage.getTotal();
    List<TbSpu> tbSpuList = spuIPage.getRecords();
    List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(tbSpuList, SpuDTO.class);
    for (SpuDTO spuDTO : spuDTOList) {
        handlerCategoryNameAndBrandName(spuDTO);
    }
    return new PageResult(total,spuDTOList);
}

    //    提取一个方法专门处理SpuDTO中的商品分类名称、品牌名称
    private SpuDTO handlerCategoryNameAndBrandName(SpuDTO spuDTO){
        //注入品牌的service查询
        TbBrand tbBrand = brandService.getById(spuDTO.getBrandId());
        spuDTO.setBrandName(tbBrand.getName());
        List<Long> categoryIds = spuDTO.getCategoryIds();//三级分类的id //注入分类的service查询
//        直接根据分类id的集合查询分类对象的集合
        Collection<TbCategory> tbCategoryCollection = categoryService.listByIds(categoryIds);
//        从这个集合中获取每个对象的名称 名称和名称之间使用/分隔、
        String categoryNames = tbCategoryCollection.stream(). //变成流
                                map(TbCategory::getName).     //获取每个对象 的名称
                                collect(Collectors.joining("/")); //收集数据，收集的时候吧名称中间用"/"分隔

        spuDTO.setCategoryName(categoryNames);

        return spuDTO;

    }

    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
//     需要保存三张表的数据
//        tb_spu  -- > spuDTO转TbSpu
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.save(tbSpu);
        Long spuId = tbSpu.getId();
//        tb_spu_detail --->  spuDTO.getSpuDetail()转TbSpuDetail
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
//        需要构建TBSPUDetail和 tbSpu的一对一关系
        tbSpuDetail.setSpuId(spuId);
        spuDetailService.save(tbSpuDetail);
//        tb_sku --->  spuDTO.getSkus() 转TbSku
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku sku : tbSkus) {
            sku.setSpuId(spuId);
        }
        skuService.saveBatch(tbSkus); //批量保存 执行的是一个SQL语句

    }

    /**
     * 商品上下架
     * @param id
     * @param saleable
     */
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void updateSpuBySpuId(Long id, Boolean saleable) {

        UpdateWrapper<TbSpu> updateWrapper=new UpdateWrapper<>();

        updateWrapper.lambda().eq(TbSpu::getId,id);
        updateWrapper.lambda().set(TbSpu::getSaleable,saleable);
        boolean update = spuService.update(updateWrapper);

        if (!update){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //修改sku的enable值
        UpdateWrapper<TbSku> updateWrapper1 =new UpdateWrapper<>();
        updateWrapper1.lambda().eq(TbSku::getSpuId,id);
        updateWrapper1.lambda().set(TbSku::getEnable,saleable);
        boolean update1 = skuService.update(updateWrapper1);
        if (!update1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //向MQ中发送消息 ITEM_UP_TAGS  ITEM_DOWN_TAGS
        //三元判断 是上架还是下架 saleable false上架 下架
        String tag=saleable?ITEM_UP_TAGS:ITEM_DOWN_TAGS;
        //将消息传到哪里 和根据id
        rocketMQTemplate.convertAndSend(ITEM_TOPIC_NAME+":"+tag,id);

        System.out.println("上下架成功,MQ消息已发送:" + tag);
    }





    /**
     * (回显)根据spuId查询spudetail的数据
     * @param id
     * @return
     */
    public SpuDetailDTO findSPUDetailBySpuId(Long id) {

        TbSpuDetail tbSpuDetail = spuDetailService.getById(id);
        //判断结果是否为空
        if (tbSpuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        SpuDetailDTO spuDetailDTO = BeanHelper.copyProperties(tbSpuDetail, SpuDetailDTO.class);


        return spuDetailDTO;
    }

    /**
     * (回显)根据spuId查询sku的数据
     * @param id
     * @return
     */
    public List<SkuDTO> findSKUBySpuId(Long id) {
            QueryWrapper<TbSku> queryWrapper =new QueryWrapper<>();
            queryWrapper.lambda().eq(TbSku::getSpuId,id);
        List<TbSku> tbSkuList = skuService.list(queryWrapper);

        //判断结果是否为空
        if (CollectionUtils.isEmpty(tbSkuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SkuDTO> skuDTOList = BeanHelper.copyWithCollection(tbSkuList, SkuDTO.class);


        return skuDTOList;
    }

    /**
     * 保存修改后的商品信息
     * @param spuDTO
     */
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        //需要保存三张表的数据
//        tb_spu  -- > spuDTO转TbSpu
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.updateById(tbSpu);
        //获取spuId
       Long spuId= tbSpu.getId();

//        tb_spu_detail --->  spuDTO.getSpuDetail()转TbSpuDetail
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
//        需要构建TBSPUDetail和 tbSpu的一对一关系
        spuDetailService.updateById(tbSpuDetail);

        //先删除原有的sku信息
        QueryWrapper<TbSku> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        skuService.remove(queryWrapper);

        //     tb_sku --->  spuDTO.getSkus() 转TbSku
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku sku : tbSkus) {
            sku.setSpuId(spuId);
        }
        skuService.saveBatch(tbSkus); //批量保存 执行的是一个SQL语句

    }

    /**
     * 根据spuId查询Spu信息
     * @param id
     * @return
     */
    public SpuDTO findSpuBySpuId(Long id) {
        TbSpu tbSpu = spuService.getById(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(tbSpu, SpuDTO.class);

        return spuDTO;
    }

    public List<SkuDTO> findSkuBySkuIds(List<Long> ids) {
        //根据Ids查询sku集合
        Collection<TbSku> tbSkus = skuService.listByIds(ids);

        //用lamda表达式将集合中的每个sku转成SkuDTO 并返回
        return tbSkus.stream().map(tbSku -> {
            return BeanHelper.copyProperties(tbSku, SkuDTO.class);
        }).collect(Collectors.toList());


    }

    /**
     * 减库存
     * @param skuIdAndNumMap
     */
    public void stockMinus(Map<Long, Integer> skuIdAndNumMap) {

        skuService.stockMinus(skuIdAndNumMap);
    }

    /**
     * 恢复库存
     * @param map
     */
    public void updateOvertimeOrderBySkuSum(Map<Long, Integer> map) {

        skuService.updateSkuStock(map);
    }
}
