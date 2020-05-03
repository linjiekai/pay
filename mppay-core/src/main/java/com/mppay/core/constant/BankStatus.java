package com.mppay.core.constant;

public enum BankStatus {
	CHECK(0, "待审核"), 
	NORMAL(1, "正常"), 
	CANCEL(2, "注销"), 
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
	
	private BankStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
