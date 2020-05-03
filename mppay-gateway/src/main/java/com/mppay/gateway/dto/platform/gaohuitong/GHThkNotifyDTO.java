package com.mppay.gateway.dto.platform.gaohuitong;

import lombok.Data;

@Data
public class GHThkNotifyDTO {
    private String Exchg_rate;
    private String agencyId;
    private String amount;
    private String bank_code;
    private String base64_memo;
    private String card_type;
    private String mer_abbr;
    private String mer_id;
    private String order_no;
    private String out_trade_no;
    private String pay_no;
    private String pay_result;//1支付成功，0未支付，2支付失败，6 已 关闭
    private String pay_time;
    private String sett_date;
    private String trade_code;
    private String sett_time;
    private String terminal_no;
    private String txn_sett_amount;
    private String user_bank_card_no;
    private String resp_code; //响应码
    private String resp_desc; //响应描述
}
