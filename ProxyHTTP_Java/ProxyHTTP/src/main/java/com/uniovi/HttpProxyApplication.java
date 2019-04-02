package com.uniovi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.content.Proxy_Config;
import com.content.Proxy_Main;

@SpringBootApplication
public class HttpProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpProxyApplication.class, args);
		
		Proxy_Config configuracionProxy = new Proxy_Config( 8080 );
		Proxy_Main proxy = new Proxy_Main( configuracionProxy );
		
		proxy.run();
	}

}
