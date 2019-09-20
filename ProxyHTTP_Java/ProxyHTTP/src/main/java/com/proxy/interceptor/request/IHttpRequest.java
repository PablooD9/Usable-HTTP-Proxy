package com.proxy.interceptor.request;

import com.proxy.interceptor.IHttpOperation;

public interface IHttpRequest extends IHttpOperation {
	byte[] getHeadersByte();
	Header getHeader(String name);
	void setHeader(String headerName, String newValue);
	
	String getMethod();
	String getRequestedResource();
	String getHttpVersion();
	int getPort();
	
	void setSSL(boolean ssl);
	boolean isSSL();
	
	void parse(String headers);
}
