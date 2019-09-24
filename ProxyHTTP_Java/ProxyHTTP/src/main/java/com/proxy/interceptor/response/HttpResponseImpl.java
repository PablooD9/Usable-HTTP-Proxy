package com.proxy.interceptor.response;

import java.util.ArrayList;
import java.util.List;

import com.proxy.interceptor.request.Header;
import com.proxy.interceptor.request.IHttpRequest;

public class HttpResponseImpl implements IHttpResponse {

	private List<Header> headers;
	private String statusLine;
	private byte[] body;
	private String host;
	private IHttpRequest request;
	
	public HttpResponseImpl() {
		headers = new ArrayList<>();
	}
	
	@Override
	public List<Header> getHeaders() {
		return headers;
	}

	@Override
	public String getStatusLine() {
		return statusLine;
	}

	@Override
	public void addHeader(Header header) {
		headers.add( header );
	}

	@Override
	public void setStatusLine(String statusLine) {
		this.statusLine = statusLine;
		System.out.println( "Status line: " + statusLine);
	}

	@Override
	public byte[] getBody() {
		return body;
	}

	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public String getHost() {
		// TODO Auto-generated method stub
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}
	
	public IHttpRequest getRequest() {
		return request;
	}

	public void setRequest(IHttpRequest request) {
		this.request = request;
	}
	

	public Header getHeader(String headerName) {
		Header headerFound = headers.parallelStream().filter(header -> header.getKey().equalsIgnoreCase(headerName))
				 .findFirst()
				 .orElse(null);

		return headerFound;
	}

	@Override
	public void setHeader(String headerName, String newValue) {
		Header header;
		if (( header=getHeader(headerName)) != null) {
			header.setValues(newValue);
		}
	}

}
