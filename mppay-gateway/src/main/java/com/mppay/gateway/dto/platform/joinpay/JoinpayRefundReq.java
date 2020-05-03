package com.mppay.gateway.dto.platform.joinpay;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: JoinpayRefundReq
 * @description: Joinpay 退款请求报文
 * @date 2020/4/21 11:41
 */
@Data
@Builder
public class JoinpayRefundReq implements Serializable {

    /**
     * 商户编号
     */
    private String p1_MerchantNo;
    /**
     * 商户原支付订
     */
    private String p2_OrderNo;
    /**
     * 商户退款订单号
     */
    private String p3_RefundOrderNo;
    /**
     * 退款金额
     */
    private String p4_RefundAmount;
    /**
     * 退款原因描述
     */
    private String p5_RefundReason;
    /**
     * 服务器异步通知地址
     */
    private String p6_NotifyUrl;
    /**
     * 退款版本号
     */
    private String q1_version;
    /**
     * 签名数据
     */
    private String hmac;
    /**
     * 退款查询版本
     */
    private String p3_Version;


    //------退款查询用的
    private String p2_RefundOrderNo;

}
