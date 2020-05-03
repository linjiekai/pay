package com.mppay.core.constant;

public enum UserOperStatus {
	CHECK(0, "待审核"), 
	NORMAL(1, "正常"), 
	CANCEL(2, "销户"), 
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
	
	private UserOperStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
