package com.mppay.core.constant;

public enum BusiType {
	DEPOSIT("01", "充值"), 
	CONSUME("02", "消费"), 
	WITHDRAW("03", "提现"), 
	INCOME("04", "收益"), 
	CASH("05", "保证金"),
	MEMBERSHIP("06", "会员套餐"),
	SUBTRACT("07", "扣减"),
	OUTDEPOSIT("08", "外部账户充值"),
	SUBTRACTSCT("09", "扣减保证金"),
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
	
	private BusiType(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
