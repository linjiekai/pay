package com.mppay.service.vo;

import lombok.Data;

@Data 
public class BankRouteVO {

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
     * 路由编号
     */
    private String routeCode;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;
    
    /**
     * 银行卡类型 01:借记卡;02:贷记卡;08:第三方平台;
     */
    private String bankCardType;
    
    /**
     * logo图标
     */
    private String logo;
}
