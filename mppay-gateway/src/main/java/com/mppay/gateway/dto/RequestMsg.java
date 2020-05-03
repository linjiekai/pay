package com.mppay.gateway.dto;

import java.util.Map;


public class RequestMsg extends BaseMsg{
	public RequestMsg() {

	}

	public RequestMsg(Map<String, Object> map) {
		this.map = map;
	}

	public RequestMsg setAttr(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public RequestMsg remove(Object key) {
		map.remove(key);
		return this;
	}
	
	
}
