package com.mppay.client.feignClient;


import com.mppay.client.dto.common.SmsDTO;
import com.mppay.client.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: Jiekai Lin
 * @Description(描述): 公共服务
 * @date: 2020/3/28 17:23
 */
@FeignClient(name = "common-api")
public interface CommonClient {


    @PostMapping("/sms/send")
    ResponseDTO sendSms(@RequestBody SmsDTO dto);
}
