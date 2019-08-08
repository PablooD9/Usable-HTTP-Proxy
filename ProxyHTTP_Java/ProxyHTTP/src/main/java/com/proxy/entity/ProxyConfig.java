package com.proxy.entity;

public class ProxyConfig {
	private static ProxyConfig instance = new ProxyConfig();
	private final int localPort = 8080;
	private final int maxNumOfClientReqWaiting = 20;
	private final String host = "localhost";
	
	private final int socketTimeOut = 120000;
	 
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
		return host;
	}
	
	int getSocketTimeOut() {
		return socketTimeOut;
	}
}
