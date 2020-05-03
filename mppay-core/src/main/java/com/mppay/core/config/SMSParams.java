package com.mppay.core.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sms")
@Data
public class SMSParams {

    private String accessKeyId;// 阿里短信AccessKeyId
    private String accessKeySecret;// 阿里短信AccessKeySecret
    private String smsTemplateBind;// 短信模板

}
