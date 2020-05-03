package com.mppay.gateway.dto.platform.sheen;

import lombok.Data;

/**
 * 查询退款状态
 * 1.0.0
 */
@Data
public class SheenRefundQueryDTO {
    /**
     * 执行结果
     */
    private String returnCode;
    /**
     * 结果描述
     */
    private String returnMsg;
    /**
     * WAITING:正在提交
     * CREATE_FAILED:提交失败
     * SUCCESS:提交成功
     * FAILED:退款失败
     * FINISHED:退款成功
     * CHANGE:退款无法到账，需要人工介入
     */
    private String resultCode;
    /**
     * SheenPay退款单号
     */
    private String refundId;
    /**
     * 商户提交的退款单号
     */
    private String partnerRefundId;
    /**
     * 退款金额，单位是货币最小单位
     */
    private int amount;
    /**
     * 币种
     */
    private String currency;
}
