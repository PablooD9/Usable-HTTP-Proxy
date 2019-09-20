package com.proxy.interceptor.response;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.interceptor.request.Header;

public interface IHttpResponse extends IHttpOperation{
	void addHeader(Header header);
	
	String getStatusLine();
	void setStatusLine(String statusLine);
}
