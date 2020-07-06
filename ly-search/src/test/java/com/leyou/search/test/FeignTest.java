package com.leyou.search.test;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 测试ly-search和ly-item之间用feign接口能否连接上
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FeignTest {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void testFeign() {
        List<BrandDTO> brandList = itemClient.findBrandListByCategoryId(76l);
        System.out.println(brandList);
    }
}
