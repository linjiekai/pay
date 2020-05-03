package com.mppay.core.constant;

public enum CardBindStatus {
	CHECK(0, "待审核"), 
	BINDING(1, "绑卡"), 
	UNBINDING(2, "解绑"), 
	FREEZE(3, "冻结"), 
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
	
	private CardBindStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
