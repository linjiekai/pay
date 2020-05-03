package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 商户退款订单表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-12-25
 */
@Data
public class MercOrderRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户退款订单号
     */
    private String mercRefundNo;

    /**
     * 商户订单号
     */
    private String mercOrderNo;

    /**
     * 商城订单号
     */
    private String refundOrderNo;

    /**
     * 商城订单号
     */
    private String orderNo;

    /**
     * 退款订单日期
     */
    private String refundDate;

    /**
     * 退款订单时间
     */
    private String refundTime;

    /**
     * 商城请求的唯一交易流水号
     */
    private String requestId;

    /**
     * 支付订单号,发往银行的订单号
     */
    private String outTradeNo;

    /**
     * 发往银行退款单号
     */
    private String outRefundNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家：MPWJMALL
     */
    private String platform;

    /**
     * 订单状态 等待退款W,退款成功S,退款失败F,BW银行退款中
     */
    private String orderStatus;

    /**
     * 支付终端系统 IOS、ANDROI、WEB、H5、WX-APPLET、WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 商城会员ID
     */
    private Long userId;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 微信或支付宝用户标识
     */
    private String openId;

    /**
     * 银行应用ID
     */
    private String appId;

    /**
     * 支付订单金额
     */
    private BigDecimal price;

    /**
     * 快捷银行卡协议号
     */
    private String agrNo;

    /**
     * 支付银行 支付银行目前仅有微信WEIXIN和支付宝ALIPAY
     */
    private String bankCode;

    /**
     * 退款金额
     */
    private BigDecimal refundPrice;

    /**
     * 客户端IP
     */
    private String clientIp;

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
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
