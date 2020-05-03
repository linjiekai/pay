package com.mppay.core.constant;

public enum RefundType {
	REFUND(0, "退款"),
	REPEAL(1, "支付撤销"),
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

	private RefundType(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
