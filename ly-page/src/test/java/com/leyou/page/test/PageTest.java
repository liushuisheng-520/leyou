package com.leyou.page.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.page.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PageTest {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private PageService pageService;
    @Autowired
    private ItemClient itemClient;

    @Test
    public void createPage() {


        int page = 1;
        while (true) {
            try {

                PageResult<SpuDTO> spuByPage = itemClient.findSpuByPage(page, 100, null, true);
                //判断是否为空
                if (spuByPage == null || spuByPage.getItems() == null) {
                    break;
                }

                List<SpuDTO> spuList = spuByPage.getItems();
                for (SpuDTO spuDTO : spuList) {
                    Long spuId = spuDTO.getId();
                    //Context:运行上下文  数据
                    Context context = new Context();
                    context.setVariables(pageService.buildDataBySpuId(spuId));

                    //TemplateResolver:模板解析器  模板文件

                    //TemplateEngine:模板引擎  把数据和模板文件结合生成一个html文件


                    try (PrintWriter writer = new PrintWriter("D:\\Java\\nginx\\nginx-1.16.0\\html\\item\\" + spuId + ".html")) {

                        templateEngine.process("item", context, writer);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

            } catch (Exception e) {
                break;
            }
            page++;
        }


    }
}
