package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化ES索引库
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchInitTest {

    @Autowired
    private ItemClient itemClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    public GoodsRepository goodsRepository;


    @Test
    public void initEs() {
        //删除所以
        goodsRepository.deleteAll();

        //定义一个页数变量
        int page = 1;
        while (true) {
            //1.分页查询所有spu数据
            PageResult<SpuDTO> spuByPage = itemClient.findSpuByPage(page, 100, null, true);
            //判断结果是否为空,是空则跳出while循环
            if (CollectionUtils.isEmpty(spuByPage.getItems())) {

                break;//查不到数据就跳出循环
            }
            //获取Spu商品中的数据
            List<SpuDTO> spuDTOList = spuByPage.getItems();

            // 创建集合放goods
            List<Goods> goodsList = new ArrayList<>();
            //2.遍历spuDTOList集合,调用searchService把spu(商品)转换成Goods存到ES中
            for (SpuDTO spuDTO : spuDTOList) {
                //调用searchService中的buildGoods方法将spu商品转换成Goods
                Goods goods = searchService.buildGoods(spuDTO);
                goodsList.add(goods);
            }

            //3.调用goodsRepository接口 将返回的Goods保存到ES中
            goodsRepository.saveAll(goodsList);

            page++;
        }


    }
}
