package com.proxy.entities;

public class ProxyConfig {
	private static ProxyConfig instance = new ProxyConfig();
	private final int puertoLocal = 8080;
	 
    private ProxyConfig(){}
 
    public static ProxyConfig getInstance()
    {
        return instance;
    }

	int getPuertoLocal() {
		return puertoLocal;
	}

}
