package com.mppay.gateway.dto.platform.sheen;

import lombok.Data;

import java.math.BigDecimal;


/*
    通用请求类
 */
@Data
public class SheenReq {

    private String description; //必填，订单标题（最大长度128字符，超出自动截取）
    private BigDecimal price; //必填，金额，单位为货币最小单位，例如使用100表示1.00 HKD
    private String currency; //币种代码  默认值: HKD 允许值: HKD, CNY
    private String channel; //支付渠道，大小写敏感    允许值: Alipay, Wechat
    private String notify_url; //支付通知url，详见支付通知api，不填则不会推送支付通知
    private String operator; //操作人员标识
    private String appid;
    private String customer_id;
    
}
