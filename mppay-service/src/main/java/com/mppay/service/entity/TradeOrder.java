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
 * 交易订单表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Data
public class TradeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 商户订单号
     */
    private String mercOrderNo;

    /**
     * 外部订单号 发往银行的订单号
     */
    private String outTradeNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 交易订单日期yyyy-MM-dd
     */
    private String tradeDate;

    /**
     * 交易订单时间HH:mm:ss
     */
    private String tradeTime;

    /**
     * 支付日期 yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间  HH:mm:ss
     */
    private String payTime;

    /**
     * 商城会员ID
     */
    private String userId;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 支付订单金额
     */
    private BigDecimal price;

    /**
     * 快捷银行卡协议号
     */
    private String agrNo;

    /**
     * 短信验证码
     */
    private String smsCode;

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
     * 银行支付订单号 由微信或者支付宝返回的银行订单号
     */
    private String bankTradeNo;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 支付终端系统 IOS、ANDROI、WEB、H5、WX-APPLET、WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 订单状态 A预登记状态,成功S,失败F,等待支付W,全额退款RF,部分退款RP
     */
    private String orderStatus;

    /**
     * 订单过期时间 YYYY-MM-DD HH:MM:SS
     */
    private String orderExpTime;

    /**
     * 已退款金额
     */
    private BigDecimal refundPrice;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;合伙人：PTMALL
     */
    private String platform;

    /**
     * 银行分配商户号
     */
    private String bankMercId;

    /**
     * 银行应用ID
     */
    private String appId;

    /**
     * 银行用户标识
     */
    private String openId;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 业务类型 01：充值;02：消费;03：提现;04：收益
     */
    private String busiType;

    /**
     * 支付类型 1：统一支付 2：网银支付 3：快捷支付
     */
    private Integer payType;

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
