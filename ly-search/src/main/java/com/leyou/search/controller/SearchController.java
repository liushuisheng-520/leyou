package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;


    //http://api.leyou.com/api/search/page

    /**
     * 根据关键字分页查询
     * @param searchRequest
     * @return
     */
    @PostMapping(value = "/page",name = "根据关键字分页查询")
    public ResponseEntity<PageResult<GoodsDTO>> findGoodsByPage(@RequestBody SearchRequest searchRequest) {

        PageResult<GoodsDTO> pageResult= searchService.findGoodsByPage(searchRequest);

        return ResponseEntity.ok(pageResult);
    }


//http://api.leyou.com/api/search/filter

    /**
     * 根据品牌Id和分类Id过滤查询
     * @param searchRequest
     * @return
     */
    @PostMapping(value = "/filter",name = "根据品牌Id和分类Id过滤查询")
    public ResponseEntity<Map<String, List<?>>> filter(@RequestBody SearchRequest searchRequest) {

        Map<String, List<?>> filterMap= searchService.filter(searchRequest);

        return ResponseEntity.ok(filterMap);
    }
}
