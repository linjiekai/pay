package com.mppay.gateway.dto;

import java.util.Map;


public class ResponseMsg extends BaseMsg {
	public ResponseMsg() {
	}

	public ResponseMsg(Map<String, Object> map) {
		this.map = map;
	}

	public ResponseMsg setAttr(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public ResponseMsg remove(Object key) {
		map.remove(key);
		return this;
	}
}
