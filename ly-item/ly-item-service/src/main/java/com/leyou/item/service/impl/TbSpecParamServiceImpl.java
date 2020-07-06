package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecParamMapper;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 规格参数组下的参数名 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbSpecParamServiceImpl extends ServiceImpl<TbSpecParamMapper, TbSpecParam> implements TbSpecParamService {

    @Override
    public List<SpecParamDTO> findSpecParamByCategoryIdOrGroupId(Long gid, Long cid, Boolean searching) {
//        gid和cid必须保持有一个值
//        判断gid和cid是否同时为空，如果是抛异常
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if (gid == null && cid == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        if (gid != null) {
//            构建gid的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getGroupId, gid);
        }
        if (cid != null) {
//            构建cid的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getCid, cid);
        }
        if (searching != null) {
//            构建searching的查询条件
            queryWrapper.lambda().eq(TbSpecParam::getSearching, searching);  //true--->1  false --->0
        }

        List<TbSpecParam> tbSpecParamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSpecParamList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSpecParamList, SpecParamDTO.class);
    }

    /**
     * 根据categoryId查询规格参数组和组内参数
     *
     * @param id
     * @return
     */
    @Autowired
    private TbSpecGroupService specGroupService;

    @Override
    public List<SpecGroupDTO> findSpecGroupWithParamsByCategoryId(Long id) {

        //查询
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda().eq(TbSpecGroup::getCid, id);
        //执行查询获取结果
        List<TbSpecGroup> tbSpecGroupList = specGroupService.list(queryWrapper);
        //判断结果是否为空
        if (CollectionUtils.isEmpty(tbSpecGroupList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        //将结果转成SpecGroup格式
        List<SpecGroupDTO> specGroupDTOS = BeanHelper.copyWithCollection(tbSpecGroupList, SpecGroupDTO.class);
        //遍历specGroupDTOList集合获取每个规格组
        /*for (SpecGroupDTO specGroupDTO : specGroupDTOList) {
            //通过规格组Id查询对应的规格参数
            List<SpecParamDTO> params = this.findSpecParamByCategoryIdOrGroupId(specGroupDTO.getId(), null, null);
            //然后将规格参数写入到specGroupDTO中
            specGroupDTO.setParams(params);
        }*/

        List<SpecParamDTO> paramList = this.findSpecParamByCategoryIdOrGroupId(null, id, null);

        Map<Long, List<SpecParamDTO>> paramMapByGroup = paramList.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));
        /*for (SpecGroupDTO specGroupDTO : specGroupDTOList) {

            List<SpecParamDTO> specParamDTOList = paramMapByGroup.get(specGroupDTO.getId());

            specGroupDTO.setParams(specParamDTOList);
        }*/

        specGroupDTOS = specGroupDTOS.stream().map(group -> {
            List<SpecParamDTO> paramDTOS = paramMapByGroup.get(group.getId());
            group.setParams(paramDTOS);
            return group;
        }).collect(Collectors.toList());
        //返回结果
        return specGroupDTOS;
    }
}
