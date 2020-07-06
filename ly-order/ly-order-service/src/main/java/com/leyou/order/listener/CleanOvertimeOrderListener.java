package com.leyou.order.listener;

import com.leyou.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ORDER_OVERTIME_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ORDER_OVERTIME_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ORDER_TOPIC_NAME;

/**
 * 创建监听器 监听MQ中的消息
 */
@RocketMQMessageListener(consumerGroup = ORDER_OVERTIME_CONSUMER, topic = ORDER_TOPIC_NAME, selectorExpression = ORDER_OVERTIME_TAGS)
@Component
public class CleanOvertimeOrderListener implements RocketMQListener<String> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(String s) {
        System.out.println(s);
        orderService.cleanOvertimeOrder();
    }
}
