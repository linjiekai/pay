package com.mppay.gateway.dto.platform.gaohuitong;


import lombok.Data;

@Data
public class GHTPreOrderDTO  {

    private String version; //固定值：2.0.0
    private String trade_code; //预下单的交易代码代码固定为PAY
    private String agencyId; //商户号
    private String child_merchant_no; //子商户号
    private String user_id; //商户系统的用户标识
    private String terminal_no; //终端号
    private String order_no; //商户订单号
    private String amount; //交易金额
    private String currency_type; //交易币种，货币代码，人民币：CNY；港币：HKD；   美元：USD
    private String sett_currency_type; //清算币种，货币代码，人民币：CNY；港币：HKD；   美元：USD
    private String product_name; //产品名称
    private String user_name; //用户名称
    private String user_cert_type; //用户证件类型 用户证件类型01:身份证；02:护照；03:其 他
    private String user_cert_no; //用户证件号码
    private String user_bank_card_no; //用户银行卡
    private String user_mobile; //用户手机号
    private String notify_url; //异步通知地址
    private String return_url; //同步通知地址
    private String client_ip; //订单生成的机器IP，指用户浏览器端IP，    不是商户服务器IP
    private String access_type; //SDK和APP端必传：0:网页|1:手 机|2:SDK|3:后台|4:【商户APP】|5:APP|6:公众号
    private String token_id; //预下单支    付授权码
    private String resp_code; //响应码
    private String resp_desc; //响应描述
    private String pay_timeout; //以秒为单位，默认18000，订单状态将改为关闭，如果用户支付成功将自动进行撤销
    private String bank_code; //银行代码
    private String mweb_url; //支付跳转 链接,bank_code= WAPWECHAT 时 返 回 ， mweb_url 为拉起微信支付 收银台的中间页面，可通过 访问该 url 来拉起微信客户 端，完成支付,mweb_url 的 有效期为 5 分钟。

}
