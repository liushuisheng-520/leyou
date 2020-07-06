package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //这个Controller包含规格组 和 规格参数的操作
@RequestMapping("/spec")
public class SpecController {
//    GET /spec/groups/of/category?id=76
//
    @Autowired
    private TbSpecGroupService specGroupService;
    @Autowired
    private TbSpecParamService specParamService;

    @GetMapping(value = "/groups/of/category",name ="根据分类id查询规格组数据" )
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam("id") Long id){
        List<SpecGroupDTO> specGroupDTOList = specGroupService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(specGroupDTOList);
    }

    @GetMapping(value = "/params",name ="根据分类id或组id查询规格参数数据" )
//    gid  cid  searching
    public ResponseEntity<List<SpecParamDTO>> findSpecParamByCategoryIdOrGroupId(
            @RequestParam(value = "gid" ,required = false) Long gid,
            @RequestParam(value ="cid" ,required = false) Long cid,
            @RequestParam(value ="searching",required = false) Boolean searching
    ){
        List<SpecParamDTO> specParamDTODTOList = specParamService.findSpecParamByCategoryIdOrGroupId(gid,cid,searching);
        return ResponseEntity.ok(specParamDTODTOList);
    }

    /**
     *根据categoryId查询规格参数组和组内参数
     * @param id
     * @return
     */
    @GetMapping(value = "/of/category",name ="根据categoryId查询规格参数组和组内参数" )
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupWithParamsByCategoryId(@RequestParam(value = "id")Long id){

        List<SpecGroupDTO> specGroupDTOList = specParamService.findSpecGroupWithParamsByCategoryId(id);

        return ResponseEntity.ok(specGroupDTOList);
    }


}
