package com.mppay.gateway.dto.platform.joinpay;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinpayResp {

    private String r0_Version;
    private String r1_MerchantNo;
    private String r2_OrderNo;
    private String r3_Amount;
    private String r4_Cur;
    private String r6_FrpCode;
    private String r7_TrxNo;
    private String ra_Code;
    private String rb_CodeMsg;
    private String rc_Result;
    private String hmac;
    private String ra_Status; //100 支付成功 101支付失败
    private String rf_PayTime;
    private String r6_BankTrxNo;
    private String rd_OpenId;
    private String rb_DealTime; //交易结果通知时间
    private String rc_BankCode; //银行编码
    private String r6_Status; //支付回调的状态码

}
