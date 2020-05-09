package com.proxy.interceptor;

public class ProxyConfigImpl implements ProxyConfig {
	private final int localPort;
	private final int maxNumOfClientReqWaiting = 1;
	private final String localhost = "localhost";
	private final int socketTimeOut;
	
	public ProxyConfigImpl(int port, int socketTO) {
		localPort = port;
		socketTimeOut = socketTO;
	}

	public int getLocalPort() {
		return localPort;
	}
	public int getMaxNumOfClientsReqWaiting() {
		return maxNumOfClientReqWaiting;
	}
	public String getLocalhost() {
		return localhost;
	}
	public int getSocketTimeOut() {
		return socketTimeOut;
	}
	
	
}
