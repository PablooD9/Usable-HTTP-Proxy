package com.proxy.interceptor.request;

import java.util.List;

public interface IHttpRequest {
//	void loadHeaders(byte[] headerBytes);
	byte[] getHeadersByte();
//	Header[] getHeaders();
	Header getHeader(String name);
	void setHeader(String headerName, String newValue);
	
	String getMethod();
	String getRequestedResource();
	String getHttpVersion();
	int getPort();
	String getHost();
	void setBody(String body);
	String getBody();
	
	void setSSL(boolean ssl);
	boolean isSSL();
	
	// ==== LO NUEVO ====
	
	void parse(String headers);
	List<Header> getHeaders();
}
