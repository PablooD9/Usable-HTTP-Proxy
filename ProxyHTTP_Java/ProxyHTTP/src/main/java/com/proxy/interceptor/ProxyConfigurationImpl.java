package com.proxy.interceptor;

public class ProxyConfigurationImpl implements IProxyConfiguration {
	private final int localPort;
	private final int maxNumOfClientReqWaiting = 1;
	private final String localhost = "localhost";
	private final int socketTimeOut;
	
	public ProxyConfigurationImpl(int port, int socketTO) {
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
