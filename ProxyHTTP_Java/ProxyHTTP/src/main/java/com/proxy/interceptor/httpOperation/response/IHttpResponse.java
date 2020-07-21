package com.proxy.interceptor.httpOperation.response;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.interceptor.httpOperation.request.Header;
import com.proxy.interceptor.httpOperation.request.IHttpRequest;

/** Clase que define las operaciones propias de una respuesta HTTP.
 * @author Pablo
 *
 */
public interface IHttpResponse extends IHttpOperation {
	/** Método que añade una cabecera a la respuesta HTTP.
	 * @param header Cabecera a añadir.
	 */
	void addHeader(Header header);

	/** Devuelve la línea de estado de la respuesta HTTP.
	 * @return Línea de estado HTTP.
	 */
	String getStatusLine();

	/** Establece la línea de estado de una respuesta HTTP.
	 * @param statusLine Línea de estado.
	 */
	void setStatusLine(String statusLine);

	/** Devuelve la referencia a la petición que originó esta respuesta HTTP.
	 * @return Petición HTTP.
	 */
	IHttpRequest getRequest();

	/** Establece la petición HTTP que originó la respuesta HTTP.
	 * @param request Petición HTTP.
	 */
	void setRequest(IHttpRequest request);
}
