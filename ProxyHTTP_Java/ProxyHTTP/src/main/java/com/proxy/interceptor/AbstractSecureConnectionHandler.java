package com.proxy.interceptor;

import javax.net.ssl.SSLContext;

public abstract class AbstractSecureConnectionHandler extends Thread implements ConnectionHandler {
	
	private String[] enabledProtocols = { "TLSv1.2", "TLSv1.1", "TLSv1" };
	
	public String[] getEnabledProtocols() {
		return enabledProtocols;
	}
	
	public abstract SSLContext createSSLContext(String host);
	
}
