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
 * 预支付交易表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class PrePayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预支付订单号
     */
    private String prePayNo;

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
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL;合伙人：PTMALL
     */
    private String platform;

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
    private String userId;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 支付订单金额
     */
    private BigDecimal price;
    
    /**
     * 随机立减
     */
    private BigDecimal reducePrice;

    /**
     * 银行编号
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
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 业务类型 01：充值;02：消费;03：提现;04：收益
     */
    private String busiType;

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
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
