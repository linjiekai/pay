package com.mppay.core.constant;

public enum TradeType {
	JSAPI("JSAPI", "公众号或小程序支付"), 
	APP("APP", "app支付"), 
	NATIVE("NATIVE", "扫码支付"), 
	MICROPAY("MICROPAY", "刷卡支付"),
	MWEB("MWEB", "H5支付"),
	QUICK("QUICK", "快捷支付"),
	PUBLIC("PUBLIC", "公众号"),
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

	private TradeType(String id, String name) {
		this.id = id;
		this.name = name;
	}

}
