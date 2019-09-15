package com.proxy.interceptor.request;

public class Header {
	private String key;
	private String values;
	
	public Header(String key, String values) {
		this.key = key;
		this.values = values;
	}

	public String getKey() {
		return key;
	}

	public String getValues() {
		return values;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public void setValues(String values) {
		this.values = values;
	}

	public String toString() {
		return key + ": " + values;
	}
	
}
