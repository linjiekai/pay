package com.mppay.gateway.dto.platform.gaohuitong;


import lombok.Data;

@Data
public class GHTCheckDTO {

    private String version; //固定值：2.0.0
    private String trade_code; //预下单的交易代码代码固定为PAY
    private String agencyId; //商户号
    private String terminal_no; //终端号
    private String settle_date; //对账日期
    private String file_type; //对账类型 ，交易对账：TRAN(默认)，退款对账：REFUND


}
