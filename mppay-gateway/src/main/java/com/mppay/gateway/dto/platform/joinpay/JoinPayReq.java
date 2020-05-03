package com.mppay.gateway.dto.platform.joinpay;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class JoinPayReq implements Serializable {

    private String p0_Version;
    private String p1_MerchantNo;
    private String p2_OrderNo;
    private BigDecimal p3_Amount;
    private String p4_Cur;
    private String p5_ProductName;
    private String p9_NotifyUrl;
    private String q1_FrpCode;
    private String q2_MerchantBankCode;
    private String q3_SubMerchantNo;
    private String q7_AppId;
    private String q5_OpenId;
    private String q9_TransactionModel;
    private String qa_TradeMerchantNo;
    private String hmac;

}
