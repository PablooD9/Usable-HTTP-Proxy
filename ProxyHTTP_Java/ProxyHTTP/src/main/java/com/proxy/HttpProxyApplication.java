package com.proxy;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.proxy.interceptor.IProxyConnection;
import com.proxy.interceptor.ProxyConnectionImpl;
import com.proxy.interceptor.certificate.SSLManager;

@SpringBootApplication
public class HttpProxyApplication {

	/** Método que arranca la aplicación.
	 * @param args Argumentos de la aplicación.
	 */
	public static void main(String[] args) {
        SpringApplication application = new SpringApplication(HttpProxyApplication.class);
		setSystemProperties(application, args);
		
		SSLManager.getInstance().generateCertificateForLocalhost();
		
		application.run(args);

		Thread t = new Thread(new Runnable() {
			 @SuppressWarnings("unused")
			public void run() {
			    Scanner keyboard = new Scanner(System.in);
			    String input=null;
			    System.out.println("Escribe 'Q' para salir / Type 'Q' to exit:");
			    while (!(input = keyboard.nextLine()).equalsIgnoreCase("q")) { // wait until the input is quit
			    	// do nothing
			    }
			    System.out.println("¡Adiós! / Bye! :)");
		        System.exit(0);
			    keyboard.close(); // don't forget to close the scanner when you are done
			 }
		}); t.start();
		
		IProxyConnection proxyConnHandler = new ProxyConnectionImpl();
		proxyConnHandler.establishConnection();
	}

	/**
	 * Establece las propiedades del sistema.
	 */
	private static void setSystemProperties(SpringApplication app, String[] args) {
		System.setProperty("restrictedHeaders", "host,connection,date,content-length,expect,upgrade"); // HttpClient does not
																							// support this headers

//		System.setProperty("javax.net.ssl.keyStore", System.getenv("JAVA_HOME") + "/lib/security/cacerts");
//		System.setProperty("javax.net.ssl.keyStore", "src/main/resources/static/security/cacerts");
//		System.setProperty("javax.net.ssl.keyStore", "static/security/cacerts");
		System.setProperty("javax.net.ssl.keyStore", "cacerts");
		System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		
		if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
			app.setAdditionalProfiles("test");
		}
	}

}
