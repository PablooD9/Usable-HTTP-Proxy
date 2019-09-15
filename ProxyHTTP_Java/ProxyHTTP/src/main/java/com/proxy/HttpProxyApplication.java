package com.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.interceptor.Proxy;
import com.proxy.interceptor.ProxyImpl;
import com.proxy.interceptor.certificate.SSLManager;

@SpringBootApplication
public class HttpProxyApplication {

	public static void main(String[] args) {
		
		SSLManager sslManager = new SSLManager();
		sslManager.createCertificates();
		
		SpringApplication.run(HttpProxyApplication.class, args);
		
		Proxy proxyConnHandler = new ProxyImpl();
		proxyConnHandler.establishConnection();
		
	}

}
