package com.proxy.entity.request;

import java.io.InputStream;

public interface HttpRequest {
	void loadHeaders(byte[] headerBytes);
	byte[] getHeadersByte();
	Header[] getHeaders();
	Header getHeader(String name);
	
	String getMethod();
	String getRequestedResource();
	String getHttpVersion();
	int getPort();
	String getHost();
	
	void setSSLConnection(boolean sslConnection);
	boolean isSSL();
	
	void setContent(InputStream content);
	InputStream getContent();
}
