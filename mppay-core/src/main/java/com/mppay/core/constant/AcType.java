package com.mppay.core.constant;

public enum AcType {
	PERSONAL("100", "账号"),
	MERC("800", "用户内部号编号"),
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
	
	private AcType(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
