package com.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.entity.Proxy;
import com.proxy.entity.ProxyImpl;
import com.proxy.entity.certificate.SSLManager;

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
