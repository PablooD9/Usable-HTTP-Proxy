package com.proxy.interceptor;

public interface ProxyConfig {

	public int getLocalPort();
	public String getLocalhost();
	public int getSocketTimeOut();
	public int getMaxNumOfClientsReqWaiting();
}
