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
 * 商户订单表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Data
public class MercOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户订单号
     */
    private String mercOrderNo;

    /**
     * 商城订单号
     */
    private String orderNo;

    /**
     * 商城订单日期yyyy-MM-dd
     */
    private String orderDate;

    /**
     * 商城订单时间HH:mm:ss
     */
    private String orderTime;

    /**
     * 商城请求的唯一交易流水号
     */
    private String requestId;

    /**
     * 支付订单号,发往银行的订单号
     */
    private String outTradeNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家：MPWJMALL
     */
    private String platform;

    /**
     * 订单状态 A预登记状态,等待付款W,付款成功S,付款失败F,全额退款RF,部分退款RP
     */
    private String orderStatus;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 支付终端系统 IOS、ANDROI、WEB、H5、WX-APPLET、WX-PUBLIC
     */
    private String sysCnl;

    /**
     * 有效期数量
     */
    private Integer period;

    /**
     * 有效期单位 00-分 01-小时 02-日 03-月
     */
    private String periodUnit;

    /**
     * 订单过期时间 YYYYMMDDHHMMSS
     */
    private String orderExpTime;

    /**
     * 联系人手机号
     */
    private String mobile;

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
     * 短信验证码
     */
    private String smsCode;

    /**
     * 支付银行 支付银行目前仅有微信WEIXIN和支付宝ALIPAY
     */
    private String bankCode;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 已退款金额
     */
    private BigDecimal refundPrice;

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
     * 支付日期 yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间 HH:mm:ss
     */
    private String payTime;

    /**
     * 通知页面交易结果时将返回到这个url
     */
    private String callbackUrl;

    /**
     * 后台通知Url
     */
    private String notifyUrl;

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
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 随机立减金额
     */
    private BigDecimal reducePrice;

}
