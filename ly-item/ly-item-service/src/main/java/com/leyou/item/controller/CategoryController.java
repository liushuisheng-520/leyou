package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private TbCategoryService categoryService;

//    of/parent?pid=0
    @GetMapping(value = "/of/parent",name = "获取属于这个父id的所有分类数据")
//    @CrossOrigin("http://manage.leyou.com") //允许manage.leyou.com"这个ip过来的请求
//    @CrossOrigin({"http://manage.leyou.com","http://www.leyou.com"}) //允许manage.leyou.com"这个ip过来的请求
    public ResponseEntity<List<CategoryDTO>> findCategoryListByParentId(@RequestParam("pid") Long pid){
        List<CategoryDTO> categoryDTOList = categoryService.findCategoryListByParentId(pid);
        return ResponseEntity.ok(categoryDTOList);
    }

    @GetMapping(value = "/of/brand/" ,name = "根据品牌id查询分类数据")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByBrandId(@RequestParam(name = "id") Long id){
        List<CategoryDTO> categoryDTOList = categoryService.findCategoryListByBrandId(id);
        return ResponseEntity.ok(categoryDTOList);
    }


    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody CategoryDTO categoryDTO){

        return ResponseEntity.ok().build();
    }

    /**
     * 根据分类Id集合查询分类数据集合
     * @param ids
     * @return
     */
    @GetMapping(value = "/list",name = "根据分类Id集合查询分类数据集合")
    public ResponseEntity<List<CategoryDTO>> findCategoryByIds(@RequestParam("ids")List<Long> ids){

        List<CategoryDTO> categoryDTOList= categoryService.findCategoryByIds(ids);

        return ResponseEntity.ok(categoryDTOList);
    }
}
