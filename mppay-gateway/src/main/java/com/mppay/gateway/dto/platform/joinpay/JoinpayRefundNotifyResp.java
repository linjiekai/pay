package com.mppay.gateway.dto.platform.joinpay;

/**
 * @author Administrator
 * @title: JoinpayRefundNotifyDTO
 * @description: 退款:应答通知参数列表
 * @date 2020/4/21 14:19
 */
public class JoinpayRefundNotifyResp {

    /**
     * 商户编号
     */
    private String r1_MerchantNo;
    /**
     * 原支付商户订单号
     */
    private String r2_OrderNo;
    /**
     * 商户退款订单号
     */
    private String r3_RefundOrderNo;
    /**
     * 退款金额
     */
    private String r4_RefundAmount_str;
    /**
     * 商户退款流水号
     */
    private String r5_RefundTrxNo;
    /**
     * 退款申请状态
     */
    private String ra_Status;
    /**
     * 响应码
     */
    private String rb_Code;
    /**
     * 响应码描述
     */
    private String rc_CodeMsg;
    /**
     * 签名数据
     */
    private String hmac;
}
