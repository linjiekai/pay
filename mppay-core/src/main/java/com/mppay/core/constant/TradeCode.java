package com.mppay.core.constant;

public enum TradeCode {
	TRADE("01", "充值"),
	CONSUMER("02", "消费"), 
	WITHDRAW("03", "提现"),
	TRADEREFUND("04", "充值退款"), 
	CONSUMERREFUND("05", "消费退款"),
	SPECIALTRADE("06", "特殊充值"),
	SPECIALREFUND(" 07", "特殊退款"),
	ADJUSTMENT("08", "调账"),
	BRANCH("09", "分账"),
	;
	
	private String id;

	private String name;

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

	private TradeCode(String id, String name) {
		this.id = id;
		this.name = name;
	}

}
