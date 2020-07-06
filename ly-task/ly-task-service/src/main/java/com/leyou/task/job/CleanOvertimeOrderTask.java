package com.leyou.task.job;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.TAGS.ORDER_OVERTIME_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ORDER_TOPIC_NAME;

@Component
public class CleanOvertimeOrderTask {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    //1小时发送一次消息
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void cleanOvertimeOrder() {

        rocketMQTemplate.convertAndSend(ORDER_TOPIC_NAME+":"+ORDER_OVERTIME_TAGS,"开始干活了");
        System.out.println("定时任务发送成功");

    }

}
