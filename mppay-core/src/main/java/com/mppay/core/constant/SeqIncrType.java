package com.mppay.core.constant;

public enum SeqIncrType {
	AC_NO("ac_no", "账号", 10),
	AGR_NO("agr_no", "快捷支付协议号", 10),
	CARD_BIND_AGR_NO("card_bind_agr_no", "提现绑卡协议号", 10),
	USER_AGR_NO("user_agr_no", "用户协议号", 10),
	USER_NO("user_no", "用户内部号编号", 10),
	USER_OPER_NO("user_oper_no", "用户操作表编号", 10),
	SMS_ORDER_NO("sms_order_no", "短信订单号", 10),
	MERC_ORDER_NO("merc_order_no", "商户订单号", 10),
	MERC_REFUND_NO("merc_refund_no", "商户退款订单号", 10),
	PRE_PAY_NO("pre_pay_no", "预支付订单号", 10),
	TRADE_NO("trade_no", "交易订单号", 10),
	REFUND_NO("refund_no", "交易退款订单号", 10),
	WITHDR_ORDER_NO("withdr_order_no", "提现订单号", 10),
	REQUEST_ID_GAOHUITONG("request_id_gaohuitong", "请求流水", 12),
	OUT_TRADE_NO_WEIXIN("out_trade_no_weixin", "微信外部订单号", 10),
	OUT_TRADE_NO_ALIPAY("out_trade_no_alipay", "支付宝外部订单号", 10),
	OUT_TRADE_NO_MPPAY("out_trade_no_mppay", "名品支付外部订单号", 10),
	OUT_TRADE_NO_GAOHUITONG("out_trade_no_gaohuitong", "高汇通外部订单号", 10),
	OUT_TRADE_NO_SHEENPAY("out_trade_no_sheenpay", "信来支付外部订单号", 10),
	OUT_REFUND_NO_WEIXIN("out_refund_no_weixin", "微信退款订单号", 10),
	OUT_REFUND_NO_ALIPAY("out_refund_no_alipay", "支付宝退款订单号", 10),
	OUT_REFUND_NO_GAOHUITONG("out_refund_no_gaohuitong", "高汇通退款订单号", 10),
	OUT_REFUND_NO_SHEENPAY("out_refund_no_sheenpay", "信来支付退款订单号", 10),
	OUT_REFUND_NO_MPPAY("out_refund_no_mppay", "名品支付退款订单号", 10),
	OUT_WITHDR_NO_WEIXIN("out_withdr_no_weixin", "微信提现外部订单号", 10),
	OUT_WITHDR_NO_ALIPAY("out_withdr_no_alipay", "支付宝提现外部订单号", 10),
	OUT_WITHDR_NO_GAOHUITONG("out_withdr_no_gaohuitong", "高汇通提现外部订单号", 10),

	
	;
	
	private String id;

	private String name;
	
	private int length;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	private SeqIncrType(String id, String name, int length) {
		this.id = id;
		this.name = name;
		this.length = length;
	}
}
