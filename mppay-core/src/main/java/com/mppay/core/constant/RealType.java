package com.mppay.core.constant;

public enum RealType {
	USER(0, "用户本人实名"),
	BUYER(1, "订购人实名"),
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

	private RealType(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
