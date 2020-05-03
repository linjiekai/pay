package com.mppay.gateway.dto.platform.joinpay;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: JoinpayQueryRefund
 * @description: TODO
 * @date 2020/4/21 14:28
 */
public class JoinpayQueryRefundResp implements Serializable {

    /**
     * 商户编号
     */
    private String r1_MerchantNo;
    /**
     * 商户退款订单号
     */
    private String r2_RefundOrderNo;
    /**
     * 退款金额
     */
    private String r3_RefundAmount;
    /**
     * 退款流水号
     */
    private String r4_RefundTrxNo;
    /**
     * 退款完成时间
     */
    private String r5_RefundCompleteTime;
    /**
     * 退款状态
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
