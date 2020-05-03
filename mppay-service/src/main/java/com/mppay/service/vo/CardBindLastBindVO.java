package com.mppay.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Administrator
 * @date 2020/3/31 17:21
 */
@Data
public class CardBindLastBindVO {

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
     * 用户ID
     */
    private Long userId;

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
     * 银行卡类型 01:借记卡;02:贷记卡;08:第三方平台;
     */
    private String bankCardType;

    /**
     * 证件类型 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private Integer cardType;

    /**
     * 证件类型名称 1:身份证 2:护照  3:军官证  4:回乡证  5:台胞证  6:港澳通行证  7:国际海员证  8:外国人永久居住证 9:其它
     */
    private String cardTypeName;

    /**
     * 身份证号
     */
    private String cardNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
