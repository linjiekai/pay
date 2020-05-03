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
 * 提现订单表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
@Data
public class WithdrOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL，合伙人：PTMALL
     */
    private String platform;

    /**
     * 提现订单号
     */
    private String withdrOrderNo;

    /**
     * 协议编号
     */
    private String agrNo;

    /**
     * 商城订单号
     */
    private String orderNo;

    /**
     * 商城订单日期
     */
    private String orderDate;

    /**
     * 商城订单时间
     */
    private String orderTime;

    /**
     * 订单流水状态 待付款:W;付款成功:S;付款失败:F
     */
    private String orderStatus;

    /**
     * 提现金额
     */
    private BigDecimal price;

    /**
     * 实际提现金额
     */
    private BigDecimal withdrPrice;

    /**
     * 提现手续费率
     */
    private BigDecimal withdrRatio;

    /**
     * 业务类型 01：充值;02：消费;03：提现;04：收益
     */
    private String busiType;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 支付终端系统 IOS、ANDROI、WEB、H5、WX-APPLET、WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     */
    private String tradeType;

    /**
     * 商户发往银行订单号
     */
    private String outTradeNo;

    /**
     * 银行预留手机号
     */
    private String mobile;

    /**
     * 性别 0:未知 1:男 2:女
     */
    private Integer gender;

    /**
     * 证件类型 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private Integer cardType;

    /**
     * 证件号
     */
    private String cardNo;

    /**
     * 证件号简称
     */
    private String cardNoAbbr;

    /**
     * 银行提现订单号
     */
    private String bankWithdrNo;

    /**
     * 支付日期 YYYY-MM-DD
     */
    private String bankWithdrDate;

    /**
     * 支付时间 HH:mm:ss
     */
    private String bankWithdrTime;

    /**
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡类型 01:借记卡;02:贷记卡;08:第三方平台;
     */
    private String bankCardType;

    /**
     * 校验用户姓名 NO_CHECK：不校验真实姓名 FORCE_CHECK：强校验真实姓名
     */
    private String checkName;
    
    /**
     * 银行商户号
     */
    private String bankMercId;

    /**
     * 机构号
     */
    private String orgNo;

    /**
     * 终端号
     */
    private String terminalNo;

    /**
     * 失效时间
     */
    private String expTime;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 备注
     */
    private String remark;

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
