package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.sms.constants.SmsConstants.*;

/*
pom.xml
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.0.3</version>
</dependency>
*/
@Component
@Slf4j
public class SendSmsUtil {
    @Autowired
    private IAcsClient client;
    @Autowired
    private SmsProperties smsProperties;

    public void sendSms(String PhoneNumbers, String SignName, String TemplateCode, String TemplateParam) {


        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(smsProperties.getDomain());
        request.setVersion(smsProperties.getVersion());
        request.setAction(smsProperties.getAction());

        request.putQueryParameter("RegionId", smsProperties.getRegionID());
        request.putQueryParameter(SMS_PARAM_KEY_PHONE, PhoneNumbers);
        request.putQueryParameter(SMS_PARAM_KEY_SIGN_NAME, SignName);
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_CODE, TemplateCode);
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_PARAM, TemplateParam);

        System.out.println("验证码:" + TemplateParam);
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            Map<String, String> responseData = JsonUtils.toMap(response.getData(), String.class, String.class);
            String message = responseData.get(SMS_RESPONSE_KEY_MESSAGE);
            //判断结果是不是OK 不是就发送短信失败
            if (!StringUtils.equals(OK, message)) {
                log.error("发送短信失败");
            }


        } catch (ServerException e) {
            log.error("服务端异常");
        } catch (ClientException e) {
            log.error("客户端异常");
        }
    }
}