package com.proxy.interceptor;

/** Interfaz que define las operaciones necesarias para configurar ciertos aspectos fundamentales
 * del Proxy.
 * @author Pablo
 *
 */
public interface IProxyConfiguration {

	/** Establece el puerto local del Proxy.
	 * @return Puerto local.
	 */
	public int getLocalPort();
	
	/** Devuelve el Host local (localhost).
	 * @return Localhost.
	 */
	public String getLocalhost();
	
	/** Devuelve el tiempo límite de espera establecido para los Sockets creados.
	 * @return Timeout de los Sockets.
	 */
	public int getSocketTimeOut();
	
	/** Devuelve el número de peticiones que se quedarán esperando en la cola
	 * a que termine la petición actual.
	 * @return Número de peticiones en la cola de espera.
	 */
	public int getMaxNumOfClientsReqWaiting();
}
