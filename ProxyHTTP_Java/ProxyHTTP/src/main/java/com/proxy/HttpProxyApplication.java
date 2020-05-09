package com.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.interceptor.Proxy;
import com.proxy.interceptor.ProxyImpl;
import com.proxy.interceptor.certificate.SSLManager;

@SpringBootApplication
public class HttpProxyApplication {

	public static void main(String[] args) {

		setSystemProperties();

		SSLManager.getInstance().createCertificateForLocalhost();

		SpringApplication.run(HttpProxyApplication.class, args);

		Proxy proxyConnHandler = new ProxyImpl();
		proxyConnHandler.establishConnection();
	}

	private static void setSystemProperties() {
//		System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,connection,content-length,expect,upgrade");
		System.setProperty("restrictedHeaders", "host,connection,content-length,upgrade"); // HttpClient does not
																							// support this headers

		// TODO CAMBIAR!!!! Crear nuestra propia keystore. CACERTS ES UNA TRUSTSTORE
		System.setProperty("javax.net.ssl.keyStore", System.getenv("JAVA_HOME") + "/lib/security/cacerts");
		System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
	}

}
