package com.mppay.core.constant;

public enum PayType {
	UNIFIED(1, "统一支付"), 
	WEB(2, "网银支付"), 
	QUICK(3, "快捷支付"), 
	;
	private int id;

	private String name;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private PayType(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
