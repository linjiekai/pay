package com.mppay.client.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsDTO {

    String templateId; //短信模板id
    String mobileCode;  //区号
    String mobile;  //手机号码
    String json;    //模版参数
    String platForm; //商户名
    String signName; //签名

}
