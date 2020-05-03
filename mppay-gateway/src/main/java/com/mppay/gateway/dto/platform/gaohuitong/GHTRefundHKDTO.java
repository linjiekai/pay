package com.mppay.gateway.dto.platform.gaohuitong;


import lombok.Data;

@Data
public class GHTRefundHKDTO {

    private String version; //固定值：2.0.0
    private String trade_code; //预下单的交易代码代码固定为PAY
    private String agencyId; //商户号
    private String child_merchant_no; //子商户号
    private String terminal_no; //终端号
    private String order_no; //商户订单号
    private String refund_no; //商户退款订单号
    private String refund_amount; //退款金额
    private String currency_type; //原交易币种，货币代码，人民币：CNY；港币：HKD；   美元：USD
    private String sett_currency_type; //原清算币种，货币代码，人民币：CNY；港币：HKD；   美元：USD

    private String resp_code; //响应码
    private String resp_desc; //响应描述
    private String refund_id; //系统退款单号
    private String refund_result; //退款结果 0未处理，1退款成功，2退款失败 ，4处理中
    private String refund_time; //退款处理时间，格式：YYYYMMDDHHMISS
    private String refund_total_amount; //累计退款金额

    private String ori_order_no; //原商户订单号
    private String pay_no; //网管系统撤销订单
    private String pay_result; //撤销结果
    private String pay_time; //撤销申请成功    时间

}
