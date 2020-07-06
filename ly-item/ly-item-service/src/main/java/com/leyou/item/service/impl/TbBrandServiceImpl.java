package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.service.TbBrandService;
import com.leyou.item.service.TbCategoryBrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Override
    public PageResult<BrandDTO> fingByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
//        sql语句：select * from tb_brand where name like '关键字' or  letter ='关键字'  order by   name  desc|asc    limit (页码-1)*每页条数 ,每页条数
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        Page<TbBrand> page1 = new Page<>(page,rows); //构建了分页
//        判断key是否有值
        if(StringUtils.isNotBlank(key)){
            queryWrapper.lambda().or().like(TbBrand::getName,key).or().eq(TbBrand::getLetter,key);
        }
        if(StringUtils.isNotBlank(sortBy)){  //判断排序字段是否为空  name  letter  image
            if(desc){
                page1.setDesc(sortBy);
            }else{
                page1.setAsc(sortBy);
            }
        }
      /*  queryWrapper.orderByAsc(sortBy);
        queryWrapper.orderByDesc(sortBy);*/
//        List<TbBrand> list = this.list(queryWrapper);
        IPage<TbBrand> iPage = this.page(page1, queryWrapper);
//        iPage.getTotal();//总条数
//        iPage.getRecords();//当前页的数据
        return new PageResult( iPage.getTotal(),iPage.getRecords());
    }

    
    @Autowired
    private TbCategoryBrandService categoryBrandService;
    @Override
    @Transactional
    public void insertBrand(BrandDTO brand, List<Long> cids) {
//        涉及到几张表
//        插入品牌数据  tbBrand
//        把BrandDTO转成TbBrand
        TbBrand tbBrand = BeanHelper.copyProperties(brand, TbBrand.class);
        this.save(tbBrand);
//        和分类产生关联  向tb_category_brand插入数据  插入几条数据？页面上有几个分类就插入几条数据
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            categoryBrand.setBrandId(tbBrand.getId());
            categoryBrand.setCategoryId(cid);
            categoryBrandService.save(categoryBrand);//保存中间表的数据
        }
    }

    @Override
    @Transactional
    public void updateBrand(BrandDTO brand, List<Long> cids) {
//        更新品牌数据  update  tb_brand
//        需要把BrandDTO装成TBBrand
        Long brandId = brand.getId();
        TbBrand tbBrand = BeanHelper.copyProperties(brand, TbBrand.class);
        this.updateById(tbBrand);
//        先删除此品牌之前关联的分类数据
//        delete from tb_category_brand where brand_id=??
        QueryWrapper<TbCategoryBrand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategoryBrand::getBrandId,brandId);
        categoryBrandService.remove(queryWrapper);
//        然后在重新新增
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            categoryBrand.setBrandId(tbBrand.getId());
            categoryBrand.setCategoryId(cid);
            categoryBrandService.save(categoryBrand);//保存中间表的数据
        }
    }

    @Override
    public List<BrandDTO> findBrandListByCategoryId(Long id) {
        List<TbBrand> tbBrandList =  this.getBaseMapper().findBrandListByCategoryId(id);
        if(CollectionUtils.isEmpty(tbBrandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbBrandList,BrandDTO.class);
    }

    /**
     * 根据品牌Id集合查询品牌信息集合
     * @param ids
     * @return
     */
    @Override
    public List<BrandDTO> findBrandByIds(List<Long> ids) {
        //根据品牌Id查询到品牌数据
        Collection<TbBrand> tbBrands = this.listByIds(ids);
        //判断结果是否为空
        if(CollectionUtils.isEmpty(tbBrands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //将获取的结果通过lamda表达式遍历 并用BeanHelper转换成BrandDTO格式 并数据返回结果
         return tbBrands.stream().map(brand -> {
            return BeanHelper.copyProperties(brand, BrandDTO.class);
        }).collect(Collectors.toList());


    }

    /**
     * 根据品牌Id查询品牌信息
     * @param id
     * @return
     */
    @Override
    public BrandDTO findBrandById(Long id) {
        TbBrand tbBrand = this.getById(id);

        BrandDTO brandDTO = BeanHelper.copyProperties(tbBrand, BrandDTO.class);


        return brandDTO;
    }
}
