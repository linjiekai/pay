package com.mppay.gateway.dto.platform.gaohuitong;


import lombok.Data;

/**
 * @author: Jiekai Lin
 * @Description(描述): 支付请求（预授权）
 * @date: 2019/12/4 15:07
 */
@Data
public class GHTPreAuthDTO {

    private String version; //固定值：2.0.0
    private String trade_code; //预下单的交易代码代码固定为PAY
    private String agencyId; //商户号
    private String bank_code;
    private String terminal_no; //终端号
    private String order_no; //商户订单号
    private String amount; //交易金额
    private String currency_type; //交易币种，货币代码，人民币：CNY；港币：HKD；   美元：USD
    private String sett_currency_type; //清算币种，货币代码，人民币：CNY；港币：HKD；   美元：USD
    private String product_name; //产品名称
    private String notify_url; //异步通知地址
    private String return_url; //同步通知地址
    private String client_ip; //订单生成的机器IP，指用户浏览器端IP，    不是商户服务器IP
    private String resp_code; //响应码
    private String resp_desc; //响应描述
    private String js_prepay_info; //微信 调起  jsApi 的信息
    private String user_bank_card_no; //
    private String scene_info; //场景信息 微信WAP支付必传
    private String pay_timeout; //以秒为单位，默认18000，订单状态将改为关闭，如果用户支付成功将自动进行撤销
    private String Exchg_rate;
    private String base64_memo;
    private String card_type;
    private String mer_abbr;
    private String mer_id;
    private String out_trade_no;
    private String pay_no;
    private String pay_result;//1支付成功，0未支付，2支付失败，6 已 关闭
    private String pay_time;
    private  String sett_date;
    private String sett_time;
    private String txn_sett_amount;

}
