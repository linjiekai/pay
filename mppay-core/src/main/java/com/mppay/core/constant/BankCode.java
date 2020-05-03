package com.mppay.core.constant;

public enum BankCode {
	WEIXIN("WEIXIN", "微信"), 
	ALIPAY("ALIPAY", "支付宝"), 
	MPPAY("MPPAY", "名品猫"), 
	UPOP("UPOP", "银联支付"),
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

	private BankCode(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static BankCode parse (String id) {
		for (BankCode obj : BankCode.values()) {
			if (obj.getId().equals(id)) {
				return obj;
			}
		}
		
		return null;
	}

}
