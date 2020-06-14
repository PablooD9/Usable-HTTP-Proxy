package com.proxy.interceptor.httpOperation.request;

import com.proxy.interceptor.IHttpOperation;

/** Interfaz utilizada para definir las operaciones propias de una petición HTTP.
 * @author Pablo
 *
 */
public interface IHttpRequest extends IHttpOperation {
	/** Devuelve el método HTTP utilizado en la petición (GET, POST, etc.).
	 * @return El método HTTP utilizado.
	 */
	String getMethod();

	/** Devuelve el recurso solicitado al Host.
	 * @return Recurso solicitado.
	 */
	String getRequestedResource();

	/** Devuelve la versión del protocolo HTTP que se envía en la petición.
	 * @return Versión HTTP.
	 */
	String getHttpVersion();

	/** Devuelve el puerto al que se dirige una petición a un Host.
	 * @return Puerto del Host al que se dirige la petición.
	 */
	int getPort();

	/** Permite definir si una petición se realiza sobre HTTPS o no.
	 * @param ssl True si la petición es hacia un Host que implementa HTTPS, False en otro caso.
	 */
	void setSSL(boolean ssl);

	/** Devuelve True si la petición se dirige a un Host que implementa HTTPS. False en otro caso.
	 * @return True si Host implementa HTTPS. False en otro caso.
	 */
	boolean isSSL();

	/** Este método permite construir una petición HTTP a partir de las cabeceras recibidas por parámetro.
	 * En el cuerpo de este método se irá completando toda la información que debe llevar una petición HTTP
	 * (versión del protocolo HTTP, método HTTP, recurso solicitado, etc.).
	 * @param headers Cabeceras a incluir en la petición.
	 */
	void buildRequest(String headers);
}
