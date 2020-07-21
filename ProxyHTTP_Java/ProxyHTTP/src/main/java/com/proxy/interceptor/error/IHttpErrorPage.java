package com.proxy.interceptor.error;

public interface IHttpErrorPage {

	/** Método que devuelve la línea de estado de una respuesta HTTP.
	 * @return Línea de estado de una respuesta HTTP.
	 */
	public byte[] getStatusLine();
	/** Método que devuelve la lista de cabeceras de una respuesta HTTP.
	 * @return Lista de cabeceras.
	 */
	public byte[] getHeaders();
	/** Método que devuelve el cuerpo de una respuesta HTTP.
	 * @return Cuerpo de una respuesta HTTP.
	 */
	public byte[] getBody();
	
}
