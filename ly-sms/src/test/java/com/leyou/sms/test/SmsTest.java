package com.leyou.sms.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.HashMap;
import java.util.Map;

import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.sms.constants.SmsConstants.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSend() {
//String PhoneNumbers=  map.get(SMS_PARAM_KEY_PHONE).toString();
//      String SignName=  map.get(SMS_PARAM_KEY_SIGN_NAME).toString();
//      String TemplateCode=  map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();
//      String TemplateParam=  map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();
        Map map = new HashMap();
        //随机产生4位数字
        String randomNumeric = RandomStringUtils.randomNumeric(4);

        map.put(SMS_PARAM_KEY_PHONE,"13290369670");
        map.put(SMS_PARAM_KEY_SIGN_NAME,"黑马旅游网");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE,"SMS_178761210");
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM,"{\"code\":\""+randomNumeric+"\"}");//注意转义字符

        //将消息放入到MQ中的topic和tags中
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME+":"+VERIFY_CODE_TAGS,map);
    }
}
