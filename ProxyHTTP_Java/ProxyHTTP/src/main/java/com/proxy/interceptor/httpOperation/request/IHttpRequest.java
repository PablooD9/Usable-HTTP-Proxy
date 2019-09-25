package com.proxy.interceptor.httpOperation.request;

import com.proxy.interceptor.IHttpOperation;

public interface IHttpRequest extends IHttpOperation {
	String getMethod();
	String getRequestedResource();
	String getHttpVersion();
	int getPort();
	
	void setSSL(boolean ssl);
	boolean isSSL();
	
	void buildRequest(String headers);
}
