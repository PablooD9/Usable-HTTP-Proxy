package com.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.handler.Proxy_Config;
import com.proxy.handler.Proxy_Main;

@SpringBootApplication
public class HttpProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpProxyApplication.class, args);
		
		Proxy_Config configuracionProxy = new Proxy_Config( 8080 );
		Proxy_Main proxy = new Proxy_Main( configuracionProxy );
		
		proxy.run();
	}

}
