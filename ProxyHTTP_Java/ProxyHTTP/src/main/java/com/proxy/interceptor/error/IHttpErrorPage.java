package com.proxy.interceptor.error;

public interface IHttpErrorPage {

	public byte[] getStatusLine();
	public byte[] getHeaders();
	public byte[] getBody();
	
}
