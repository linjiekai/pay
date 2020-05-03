package com.mppay.core.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SMSConfig {

    @Autowired
    private SMSParams smsParams;

    @Bean
    @Primary
    public IAcsClient iAcsClient() {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                smsParams.getAccessKeyId(),
                smsParams.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        return client;
    }
}
