package com.mppay.gateway.dto.platform.joinpay;

import java.io.Serializable;

/**
 * @author Administrator
 * @title: JoinpayQueryRefund
 * @description: 查询退款
 * @date 2020/4/21 14:28
 */
public class JoinpayQueryRefundReq implements Serializable {

    /**
     * 商户编号
     */
    private String p1_MerchantNo;
    /**
     * 商户退款订单号
     */
    private String p2_RefundOrderNo;
    /**
     * 版本号
     */
    private String p3_Version;
    /**
     * 签名数据
     */
    private String hmac;

}
