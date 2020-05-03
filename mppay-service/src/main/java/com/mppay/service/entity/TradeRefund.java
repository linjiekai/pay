package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 交易退款明细表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class TradeRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款订单号
     */
    private String refundNo;

    /**
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 支付时发往银行的支付流水号
     */
    private String outTradeNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 商户订单号
     */
    private String mercOrderNo;

    /**
     * 商户退款订单号
     */
    private String mercRefundNo;

    /**
     * 发往银行退款单号
     */
    private String outRefundNo;

    /**
     * 退款申请日期yyyy-MM-dd
     */
    private String refundDate;

    /**
     * 退款申请时间HH:mm:ss
     */
    private String refundTime;

    /**
     * 银行退款订单号
     */
    private String bankRefundNo;

    /**
     * 银行支付订单号
     */
    private String bankTradeNo;

    /**
     * 支付银行
     */
    private String bankCode;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 资金银行
     */
    private String fundBank;

    /**
     * 退款订单状态 退款成功S,退款失败F,等待退款RW,退款登记R,银行受理中BW
     */
    private String orderStatus;

    /**
     * 申请退款金额
     */
    private BigDecimal applyPrice;

    /**
     * 实际退款金额
     */
    private BigDecimal actualPrice;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 银行返回退款结果日期yyyy-MM-dd
     */
    private String bankReturnDate;

    /**
     * 银行返回退款结果时间HH:mm:ss
     */
    private String bankReturnTime;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 返回码
     */
    private String returnCode;

    /**
     * 返回信息
     */
    private String returnMsg;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
