package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private TbBrandService brandService;

    @GetMapping(value = "/page",name = "分页查询品牌数据")
    public ResponseEntity<PageResult<BrandDTO>> findByPage(
            @RequestParam(value = "key",required = false)String key, //关键
            @RequestParam(value = "page",defaultValue = "1")Integer page, //当前页码
            @RequestParam(value = "rows",defaultValue="10")Integer rows, //每页显示的条数
            @RequestParam(value = "sortBy",required = false)String sortBy, //排序字段
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc //是否降序
            ){
        PageResult<BrandDTO> pageResult = brandService.fingByPage(key,page,rows,sortBy,desc);
        return ResponseEntity.ok(pageResult);
    }


    @PostMapping //新增方法
    public ResponseEntity<Void> insertBrand(BrandDTO brand, @RequestParam("cids") List<Long> cids){ //使用字符串  数组 集合都可以接收
        brandService.insertBrand(brand,cids);
        return ResponseEntity.ok().build();
    }

    @PutMapping  //修改方法
    public ResponseEntity<Void> updateBrand(BrandDTO brand, @RequestParam("cids") List<Long> cids){ //使用字符串  数组 集合都可以接收
        brandService.updateBrand(brand,cids);
        return ResponseEntity.ok().build();
    }

//    GET /brand/of/category?id=76
    @GetMapping (value = "/of/category",name = "根据分类id查询品牌数据")
    public ResponseEntity<List<BrandDTO>> findBrandListByCategoryId(@RequestParam("id") Long id){
        List<BrandDTO> brandDTOList = brandService.findBrandListByCategoryId(id);
        return ResponseEntity.ok(brandDTOList);
    }

    /**
     * 根据品牌Id集合查询品牌信息集合
     * @param ids
     * @return
     */
    @GetMapping(value = "/list",name = "根据品牌Id查询品牌信息")
    public ResponseEntity<List<BrandDTO>> findBrandByIds(@RequestParam("ids") List<Long> ids){
        List<BrandDTO> brandDTOList= brandService.findBrandByIds(ids);

        return ResponseEntity.ok(brandDTOList);
    }

    /**
     * 根据品牌Id查询品牌信息
     * @param
     * @return
     */
    @GetMapping(value = "/{id}",name = "根据品牌Id查询品牌信息")
    public ResponseEntity<BrandDTO> findBrandById(@PathVariable("id") Long id){
        BrandDTO brandDTO = brandService.findBrandById(id);

        return ResponseEntity.ok(brandDTO);
    }



}
