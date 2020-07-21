package com.proxy.interceptor;

/** Interfaz que define la operación necesaria para realizar la configuración 
 * de la conexión al servidor Proxy.
 * @author Pablo
 *
 */
public interface IProxyConnection {
	/**
	 * Establece los parámetros de conexión al servidor Proxy (puerto de escucha, Host local, tiempo
	 * límite de espera, etc.).
	 */
	void establishConnection();
}
