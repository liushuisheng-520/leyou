package com.leyou.sms.listener;

import com.leyou.sms.utils.SendSmsUtil;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.SMS_VERIFY_CODE_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.sms.constants.SmsConstants.*;

@Component
@RocketMQMessageListener(consumerGroup = SMS_VERIFY_CODE_CONSUMER,topic = SMS_TOPIC_NAME,selectorExpression = VERIFY_CODE_TAGS)
public class SmsListener implements RocketMQListener<Map> {

    @Autowired
    private SendSmsUtil sendSmsUtil;

    @Override
    public void onMessage(Map map) {
//public static final String SMS_PARAM_KEY_PHONE = "PhoneNumbers";
//    public static final String SMS_PARAM_KEY_SIGN_NAME = "SignName";
//    public static final String SMS_PARAM_KEY_TEMPLATE_CODE = "TemplateCode";
//    public static final String SMS_PARAM_KEY_TEMPLATE_PARAM= "TemplateParam";

      String PhoneNumbers=  map.get(SMS_PARAM_KEY_PHONE).toString();
      String SignName=  map.get(SMS_PARAM_KEY_SIGN_NAME).toString();
      String TemplateCode=  map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();
      String TemplateParam=  map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();

      sendSmsUtil.sendSms(PhoneNumbers,SignName,TemplateCode,TemplateParam);
        System.out.println("短信发送成功");
    }
}
