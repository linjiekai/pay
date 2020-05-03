package com.mppay.gateway.dto.platform.sheen;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SheenNotifyDTO implements Serializable {
    /**
     * UTC时间戳
     */
    private long time;
    /**
     * 随机字符串
     */
    @JSONField(name = "nonce_str")
    private String nonceStr;
    /**
     * 签名
     */
    private String sign;
    /**
     * 商户订单ID
     */
    @JSONField(name = "partner_order_id")
    private String partnerOrderId;
    /**
     * SheenPay订单ID
     */
    @JSONField(name = "order_id")
    private String orderId;
    /**
     * 订单金额，单位是最小货币单位
     */
    @JSONField(name = "total_fee")
    private int totalFee;
    /**
     * 支付金额，单位是最小货币单位
     */
    @JSONField(name = "real_fee")
    private int realFee;
    /**
     * 交易时使用的汇率，1HKD=?CNY
     */
    private Double rate;
    /**
     * 客户ID
     */
    @JSONField(name = "customer_id")
    private String customerId;
    /**
     * 币种，HKD
     */
    @JSONField(name = "order_id")
    private String currency;
    /**
     * 支付渠道 Alipay|支付宝、Wechat|微信、Bestpay|翼支付
     */
    private String channel;
    /**
     * 订单创建时间，格式为'yyyy-MM-dd HH:mm:ss'，GMT+8
     */
    @JSONField(name = "create_time")
    private Date createTime;
    /**
     * 订单支付时间，格式为'yyyy-MM-dd HH:mm:ss'，GMT+8
     */
    @JSONField(name = "pay_time")
    private Date payTime;

}
