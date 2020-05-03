package com.mppay.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardBindVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 协议号
     */
    private String agrNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL，合伙人：PTMALL
     */
    private String platform;

    /**
     * 用户操作号
     */
    private String userOperNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 卡状态 1：绑定 2：解绑 3：冻结
     */
    private Integer status;

    /**
     * 绑定日期 YYYY-MM-DD
     */
    private String bindDate;

    /**
     * 绑定时间 HH:mm:ss
     */
    private String bindTime;

    /**
     * 银行简称
     */
    private String bankAbbr;

    /**
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    private String logo;

    /**
     * 银行卡类型 0:第三方平台;1:银行;9:未知
     */
    private Integer bankType;

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
     * 开户省代码
     */
    private String bankProv;

    /**
     * 开户市代码
     */
    private String bankCity;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 备注
     */
    private String remark;

    /**
     * 身份证号
     */
    private String cardNo;

    /**
     * 短信订单号
     */
    private String smsOrderNo;

    /**
     * 是否需要发送短信: Y：需要 N：不需要
     */
    private String needSms;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
