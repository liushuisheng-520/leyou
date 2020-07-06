package com.leyou.page.listenter;

import com.leyou.page.service.PageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ITEM_PAGE_UP_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

//监听器
@Component
@RocketMQMessageListener(consumerGroup = ITEM_PAGE_UP_CONSUMER, topic = ITEM_TOPIC_NAME, selectorExpression = ITEM_UP_TAGS)
public class ItemUpListener implements RocketMQListener<Long> {
    @Autowired
    private PageService pageService;

    @Override
    public void onMessage(Long spuId) {

        pageService.createHtml(spuId);

    }
}
