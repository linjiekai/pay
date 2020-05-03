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
 * 短信订单表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-11
 */
@Data 
public class SmsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 快捷签约协议号
     */
    private String agrNo;

    /**
     * 签约类型 01:快捷
     */
    private String agrType;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 商城用户号
     */
    private Long userId;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 卡状态 0:待绑定 1：绑定成功
     */
    private Integer status;

    /**
     * 快捷路由编号
     */
    private String routeCode;

    /**
     * 订单号
     */
    private String smsOrderNo;

    /**
     * 订单类型 0-签约+支付验证码 1-签约验证码 2-支付验证码 3-解约验证码 4-提现绑卡验证码 5-提现验证码
     */
    private Integer smsOrderType;

    /**
     * 短信验证码
     */
    private String smsCode;

    /**
     * 支付订单金额
     */
    private BigDecimal price;

    /**
     * 短信流水
     */
    private String smsRequestId;

    /**
     * 银行绑定订单号
     */
    private String bindOrderNo;

    /**
     * 银行绑定协议号
     */
    private String bindAgrNo;

    /**
     * 发往银行的订单号
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
     * 绑定日期 YYYY-MM-DD
     */
    private String bindDate;

    /**
     * 绑定时间 HH:mm:ss
     */
    private String bindTime;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 银行卡类型 01:借记卡;02:贷记卡
     */
    private String bankCardType;
    
    /**
     * 失效时间
     */
    private String expTime;

    /**
     * 是否需要短信验证 Y：需要 N：不需要
     */
    private String needSms;

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
