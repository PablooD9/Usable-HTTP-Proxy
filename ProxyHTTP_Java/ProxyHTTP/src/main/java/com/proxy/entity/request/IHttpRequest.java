package com.proxy.entity.request;

import java.util.List;

public interface IHttpRequest {
//	void loadHeaders(byte[] headerBytes);
	byte[] getHeadersByte();
//	Header[] getHeaders();
	Header getHeader(String name);
	
	String getMethod();
	String getRequestedResource();
	String getHttpVersion();
	int getPort();
	String getHost();
	
	void setSSL(boolean ssl);
	boolean isSSL();
	
	// ==== LO NUEVO ====
	
	void parse(String headers);
	List<Header> getHeaders();
}
