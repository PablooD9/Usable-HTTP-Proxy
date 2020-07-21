package com.proxy.interceptor;

import javax.net.ssl.SSLContext;

/** Clase abstracta que define los protocolos de cifrado habilitados para las comunicaciones seguras
 * mediante SSL.
 * @author Pablo
 *
 */
public abstract class AbstractSecureConnectionHandler extends Thread implements ConnectionHandler {
	
	private String[] enabledProtocols = { "TLSv1.2", "TLSv1.1", "TLSv1" };
	
	public String[] getEnabledProtocols() {
		return enabledProtocols;
	}
	
	/** Devuelve un contexto SSL que se usará para las comunicaciones con hosts que implementen HTTPS, 
	 * donde se podrá encontrar los protocolos de cifrado habilitados, los certificados de confianza,
	 * entre otras propiedades.
	 * @param host Host para el que se entablará comunicación.
	 * @return Contexto SSL para la comunicación.
	 */
	public abstract SSLContext createSSLContext(String host);
	
}
