package com.mppay.gateway.dto.platform.joinpay;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Administrator
 * @title: JoinpayRefundReq
 * @description: Joinpay 退款响应报文
 * @date 2020/4/21 11:41
 */
@Data
public class JoinpayRefundResp implements Serializable {

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
    private BigDecimal r4_RefundAmount;
    /**
     * 退款金额 Str
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




    //-------退款查询的参数
    /**
     * 退款完成时间
     */
    private String r5_RefundCompleteTime;
    /**
     * 退款金额
     */
    private BigDecimal r3_RefundAmount;
    /**
     * 商户退款订单号
     */
    private String r2_RefundOrderNo;
    /**
     * 退款流水号
     */
    private String r4_RefundTrxNo;

}
