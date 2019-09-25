package com.proxy.interceptor.httpOperation.response;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.interceptor.httpOperation.request.Header;
import com.proxy.interceptor.httpOperation.request.IHttpRequest;

public interface IHttpResponse extends IHttpOperation{
	void addHeader(Header header);
	
	String getStatusLine();
	void setStatusLine(String statusLine);

	IHttpRequest getRequest();
	void setRequest(IHttpRequest request);
}
