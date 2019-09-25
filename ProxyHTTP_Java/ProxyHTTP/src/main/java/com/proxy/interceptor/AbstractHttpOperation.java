package com.proxy.interceptor;

import java.util.ArrayList;
import java.util.List;

import com.proxy.interceptor.httpOperation.request.Header;

public abstract class AbstractHttpOperation implements IHttpOperation {

	private String host;
	private byte[] body;
	private List<Header> headers;
	
	public AbstractHttpOperation() {
		headers = new ArrayList<>();
	}
	
	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	public List<Header> getHeaders() {
		return headers;
	}
	
	@Override
	public Header getHeader(String name) {
		Header headerFound = headers.parallelStream().filter(header -> header.getKey().equalsIgnoreCase(name))
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

	@Override
	public byte[] getBody() {
		return body;
	}

	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

}
