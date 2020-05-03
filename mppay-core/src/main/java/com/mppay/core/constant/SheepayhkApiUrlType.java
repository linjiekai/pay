package com.mppay.core.constant;

public enum SheepayhkApiUrlType {
	WXAPPLET("WX-APPLET", "gateway"),
	WXPUBLIC("WX-PUBLIC", "jsapi_gateway"),
	ANDROID("ANDROID", "gateway"),
	IOS("IOS", "gateway"),
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

	private SheepayhkApiUrlType(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static SheepayhkApiUrlType parasByName(String id) {
        for (SheepayhkApiUrlType obj : SheepayhkApiUrlType.values()) {
            if (obj.getId().equalsIgnoreCase(id)) {
                return obj;
            }
        }
        return null;
    }
	
	public static String getName(String id) {
        for (SheepayhkApiUrlType obj : SheepayhkApiUrlType.values()) {
            if (obj.getId().equalsIgnoreCase(id)) {
                return obj.getName();
            }
        }
        return null;
    }

	public static String getApiUrl(String id, String partnerCode) {
		String url = "";
        for (SheepayhkApiUrlType obj : SheepayhkApiUrlType.values()) {
            if (obj.getId().equalsIgnoreCase(id)) {
            	url = obj.getName()  + "/partners/" + partnerCode + "/orders/";
            	if (SheepayhkApiUrlType.WXAPPLET.getId().equals(obj.getId())) {
            		url = obj.getName()  + "/partners/" + partnerCode + "/microapp_orders/";
            	} else if (SheepayhkApiUrlType.ANDROID.getId().equals(obj.getId()) || SheepayhkApiUrlType.IOS.getId().equals(obj.getId()) ) {
            		url = obj.getName()  + "/partners/" + partnerCode + "/app_orders/";
            	}
            }
        }
        return url;
    }
}
