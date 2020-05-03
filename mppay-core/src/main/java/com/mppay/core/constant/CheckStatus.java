package com.mppay.core.constant;

public enum CheckStatus {
	WAITCHECK("0", "待对账"), 
	SUCCESS("1", "成功"), 
	SHORT("2", "短款/我方有,对方无"), 
	LONG("3", "长款/对方有,我方无"), 
	DIFF("4", "金额差错"), 
	DOUBT("5", "存疑"), 
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
	
	private CheckStatus(String id, String name) {
		this.id = id;
		this.name = name;
	}

}
