package com.mppay.gateway.dto.platform.sheen;

import java.util.Map;

import lombok.Data;


/*
    通用相应类
 */
@Data
public class SheenResp {

    private String return_code; //执行结果
    private String result_code; //SUCCESS表示创建订单成功，EXISTS表示订单已存在
    private String partner_code; //商户编码
    private String channel; //支付渠道，大小写敏感    允许值: Alipay, Wechat
    private String full_name; //商户注册全名
    private String partner_name; //商户名称
    private String order_id; //SheenPay订单ID，同时也是微信订单ID，最终支付成功的订单ID可能不同
    private String partner_order_id; //商户订单号
    private String pay_url; //跳转URL
    private String return_msg; //错误码描述
    private Integer total_fee; //订单金额，单位是货币最小面值单位
    private Integer real_fee; //实际支付金额，单位是货币最小面值单位(目前等于订单金额，为卡券预留)
    private Double rate; //交易时使用的汇率，1HKD=?CNY
    private String customer_id; //客户ID
    private String pay_time; //支付时间（yyyy-MM-dd HH:mm:ss，GMT+8）
    private String create_time; //订单创建时间（最新订单为准）（yyyy-MM-dd HH:mm:ss，GMT+8）
    private String currency; //币种，通常为HKD
    private Map<String, Object> sdk_params;
}
