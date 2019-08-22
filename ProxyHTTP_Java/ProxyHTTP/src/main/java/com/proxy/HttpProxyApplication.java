package com.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.entity.DesignTest;
import com.proxy.entity.certificate.SSLManager;

@SpringBootApplication
public class HttpProxyApplication {

	public static void main(String[] args) {
		
		new SSLManager();
		
		SpringApplication.run(HttpProxyApplication.class, args);
		
		DesignTest proxy = new DesignTest();
		proxy.start();
		
		/*
		Proxy_Config configuracionProxy = new Proxy_Config( 8080 );
		Proxy_Main proxy = new Proxy_Main( configuracionProxy );
		
		proxy.run();
		*/
		
	}

}
