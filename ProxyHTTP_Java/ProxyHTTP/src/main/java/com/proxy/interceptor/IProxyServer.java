package com.proxy.interceptor;

import java.net.Socket;

/** Interfaz que define los métodos necesarios para lanzar el servidor Proxy.
 * @author Pablo
 *
 */
public interface IProxyServer {
	/**
	 * Arranca el servidor Proxy.
	 */
	public void runServer();
	
	/** Configura los Sockets creados durante las intercepciones del servidor Proxy.
	 * @param socket Socket a configurar.
	 */
	public void configureSocket(Socket socket);
}
