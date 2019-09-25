package com.proxy.interceptor.httpOperation.response;

import com.proxy.interceptor.AbstractHttpOperation;
import com.proxy.interceptor.httpOperation.request.Header;
import com.proxy.interceptor.httpOperation.request.IHttpRequest;

public class HttpResponseImpl extends AbstractHttpOperation implements IHttpResponse {

	private String statusLine;
	private IHttpRequest request;
	
	public HttpResponseImpl() {
		super();
	}

	@Override
	public String getStatusLine() {
		return statusLine;
	}

	@Override
	public void addHeader(Header header) {
		getHeaders().add( header );
	}

	@Override
	public void setStatusLine(String statusLine) {
		this.statusLine = statusLine;
		System.out.println( "Status line: " + statusLine);
	}
	
	public IHttpRequest getRequest() {
		return request;
	}

	public void setRequest(IHttpRequest request) {
		this.request = request;
	}

}
