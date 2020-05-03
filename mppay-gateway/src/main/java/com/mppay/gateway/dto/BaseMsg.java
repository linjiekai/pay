package com.mppay.gateway.dto;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class BaseMsg {

	/** 请求、响应都是利用map来设置和获取值 */
	protected Map<String, Object> map = new HashMap<String, Object>();

	public int size() {
		return map.size();
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	public void put(String key, Object value) {
		map.put(key, value);
	}
	
	public void putAll(Map<String, Object> params) {
		if (null != params && params.size() > 0) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (!StringUtils.isBlank(entry.getKey()) && null != entry.getValue() && !StringUtils.isBlank(entry.getValue().toString())) {
					map.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	
	public void clear() {
		map.clear();
	}
	
	@SuppressWarnings("rawtypes")
	public Map getMap() {
		return map;

	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}
