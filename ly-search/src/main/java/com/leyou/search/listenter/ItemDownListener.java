package com.leyou.search.listenter;


import com.leyou.search.service.SearchService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ITEM_SEARCH_DOWN;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

//监听器
@Component
@RocketMQMessageListener(consumerGroup = ITEM_SEARCH_DOWN, topic = ITEM_TOPIC_NAME, selectorExpression = ITEM_DOWN_TAGS)
public class ItemDownListener implements RocketMQListener<Long> {
    @Autowired
    private SearchService searchService;

    @Override
    public void onMessage(Long spuId) {

        searchService.removeGoods(spuId);

    }
}
