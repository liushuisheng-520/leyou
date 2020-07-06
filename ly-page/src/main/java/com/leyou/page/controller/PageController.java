package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;
//http://www.leyou.com/item/88.html

    @GetMapping(value = "/item/{id}.html", name = "获取页面路径")
    public String pageDetail(Model model, @PathVariable("id") Long id) {

      Map map= pageService.buildDataBySpuId(id);

      model.addAllAttributes(map);

        return "item";

    }
}
