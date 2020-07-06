package com.leyou.page.service;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;

    public Map buildDataBySpuId(Long id) {

        Map mapData = new HashMap();

        SpuDTO spu = itemClient.findSpuBySpuId(id);

        //spuName spu对象的名称
        mapData.put("spuName", spu.getName());

        //subTitle spu对象的子标题
        mapData.put("subTitle", spu.getSubTitle());

        //categories spu三级分类集合
        List<Long> categoryIds = spu.getCategoryIds();

        List<CategoryDTO> categorylist = itemClient.findCategoryByIds(categoryIds);
        mapData.put("categories", categorylist);

        //brand 品牌的对象
        Long brandId = spu.getBrandId();

        BrandDTO brand = itemClient.findBrandById(brandId);
        mapData.put("brand", brand);

        //detail  spuDetail对象
        SpuDetailDTO spuDetail = itemClient.findSPUDetailBySpuId(id);
        mapData.put("detail", spuDetail);

        //skus  当前spu下的sku集合
        List<SkuDTO> skuBySpuId = itemClient.findSKUBySpuId(id);
        mapData.put("skus", skuBySpuId);

        //specs 规格组的集合,每个规格组下都所属的规格参数的集合
        List<SpecGroupDTO> specGroupDTOS = itemClient.findSpecGroupWithParamsByCategoryId(spu.getCid3());
        mapData.put("specs", specGroupDTOS);


        return mapData;
    }

    /**
     * 下架删除静态页面
     *
     * @param spuId
     */
    public void removeHtml(Long spuId) {

        File file = new File("D:\\Java\\nginx\\nginx-1.16.0\\html\\item\\" + spuId + ".html");

        file.delete();

        System.out.println("静态页面删除成功");
    }

    /**
     * 上架添加静态页面
     *
     * @param spuId
     */
    @Autowired
    private TemplateEngine templateEngine;

    public void createHtml(Long spuId) {

        Context context = new Context();
        context.setVariables(this.buildDataBySpuId(spuId));

        //TemplateResolver:模板解析器  模板文件

        //TemplateEngine:模板引擎  把数据和模板文件结合生成一个html文件


        try (PrintWriter writer = new PrintWriter("D:\\Java\\nginx\\nginx-1.16.0\\html\\item\\" + spuId + ".html")) {

            templateEngine.process("item", context, writer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("静态页面创建成功");

    }
}
