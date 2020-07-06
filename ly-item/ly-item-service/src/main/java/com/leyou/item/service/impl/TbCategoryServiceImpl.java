package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.service.TbCategoryService;
import com.leyou.common.utils.BeanHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    @Override
    public List<CategoryDTO> findCategoryListByParentId(Long pid) {
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategory::getParentId,pid);
        List<TbCategory> tbCategoryList = this.list(queryWrapper);
//        判断是否有数据
        if(CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
//        把TBCategoryList转成CategoryDTOLIst
        List<CategoryDTO> categoryDTOList = BeanHelper.copyWithCollection(tbCategoryList, CategoryDTO.class);
        return categoryDTOList;
    }

    @Override
    public List<CategoryDTO> findCategoryListByBrandId(Long id) {
//        select c.*  from tb_category_brand cb ,tb_category c
//        where cb.category_id=c.id and cb.brand_id=325400
       List<TbCategory> tbCategoryList = this.getBaseMapper().findCategoryListByBrandId(id);

       if(CollectionUtils.isEmpty(tbCategoryList)){
           throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
       }
        List<CategoryDTO> categoryDTOList = BeanHelper.copyWithCollection(tbCategoryList, CategoryDTO.class);
        return categoryDTOList;
    }

    /**
     * 根据分类Id集合查询分类数据集合
     * @param ids
     * @return
     */
    @Override
    public List<CategoryDTO> findCategoryByIds(List<Long> ids) {
        //根据分类Id集合查询分类数据集合
        Collection<TbCategory> tbCategories = this.listByIds(ids);
        //用lamda表达式遍历 再用BeanHelper转换成CategoryDTO格式
        List<CategoryDTO> categoryDTOList = tbCategories.stream().map(category -> {
            return BeanHelper.copyProperties(category, CategoryDTO.class);
        }).collect(Collectors.toList());//收集返回结果


        return categoryDTOList;
    }
}
