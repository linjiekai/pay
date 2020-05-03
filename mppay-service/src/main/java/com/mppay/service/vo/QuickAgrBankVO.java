package com.mppay.service.vo;

import lombok.Data;

@Data
public class QuickAgrBankVO {

    /**
     * 快捷签约协议号
     */
    private String agrNo;

    /**
     * 签约类型 01:快捷
     */
    private String agrType;

    /**
     * 银行号
     */
    private String bankCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行简称
     */
    private String bankAbbr;

    /**
     * 状态 0:审核 1:正常 2:冻结
     */
    private Integer status;

    /**
     * 银行类型 0:第三方平台;1:银行;9:未知
     */
    private Integer bankType;

    /**
     * 银行卡类型 01:借记卡;02:贷记卡;05:银联支付;08:第三方平台(微信和支付宝);
     */
    private String bankCardType;

    /**
     * logo图标
     */
    private String logo;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 银行卡号
     */
    private String bankCardNo;
}
