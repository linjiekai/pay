package com.mppay.gateway.sms;


import com.mppay.MppayGatewayApplication;
import com.mppay.client.dto.ResponseDTO;
import com.mppay.client.dto.common.SmsDTO;
import com.mppay.client.feignClient.CommonClient;
import com.mppay.core.config.SMSParams;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.exception.BusiException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MppayGatewayApplication.class)
@Slf4j
public class AliSMSTest {

    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    protected SMSParams smsParams;
    @Autowired
    protected CommonClient commonClient;

    @Test
    public void sendSms() throws Exception {
        SmsDTO smsDTO = SmsDTO.builder().mobile("13509030019").mobileCode("86").json("{\"code\":\"1234\"}").platForm(PlatformType.XFYLMALL.getCode()).templateId(smsParams.getSmsTemplateBind()).build();
        ResponseDTO responseDTO = commonClient.sendSms(smsDTO);
        if (!ConstEC.SUCCESS_10000.equals(responseDTO.getCode())) {
            log.error("|提现绑卡短信|获取绑卡信息|发送短信失败|");
            throw new BusiException("31025");
        }
    }
}
