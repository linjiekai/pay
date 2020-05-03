package com.mppay.core.constant;

public enum BankCardType {
	DEBIT("01", "借记卡"), 
	CREDIT("02", "贷记卡"), 
	UPOP("05", "银联支付"), 
	THIRD("08", "第三方"), 
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
	
	private BankCardType(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
