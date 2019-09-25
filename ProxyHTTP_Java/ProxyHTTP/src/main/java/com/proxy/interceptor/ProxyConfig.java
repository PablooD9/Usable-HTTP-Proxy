package com.proxy.interceptor;

public class ProxyConfig {
	private static ProxyConfig instance = new ProxyConfig();
	private final int localPort = 8080;
	private final int maxNumOfClientReqWaiting = 20;
	private final String localhost = "localhost";
	
	private final int socketTimeOut = 18000;
	 
    private ProxyConfig(){}
 
    public static ProxyConfig getInstance()
    {
        return instance;
    }

	int getLocalPort() {
		return localPort;
	}

	int getMaxNumOfClientsReqWaiting() {
		return maxNumOfClientReqWaiting;
	}
	
	String getHost() {
		return localhost;
	}
	
	int getSocketTimeOut() {
		return socketTimeOut;
	}
}
