package com.mppay.core.constant;

public enum SysCnlType {
	WX_PUBLIC("WX-PUBLIC", "微信公众号"),
	WX_APPLET("WX-APPLET", "微信小程序"),
	IOS("IOS", "IOS"),
	ANDROID("ANDROID", "安卓"),
	H5("H5", "H5"),
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

	private SysCnlType(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public static SysCnlType parse (String id) {
		for (SysCnlType obj : SysCnlType.values()) {
			if (obj.getId().equals(id)) {
				return obj;
			}
		}
		return null;
	}


}
