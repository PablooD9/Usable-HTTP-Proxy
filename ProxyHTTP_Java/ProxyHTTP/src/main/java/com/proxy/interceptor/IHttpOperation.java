package com.proxy.interceptor;

import java.util.List;

import com.proxy.interceptor.httpOperation.request.Header;

public interface IHttpOperation {
	String getHost();
	void setHost(String host);
	
	Header getHeader(String name);
	List<Header> getHeaders();
	void setHeader(String headerName, String newValue);
	
	byte[] getBody();
	void setBody(byte[] body);
}
