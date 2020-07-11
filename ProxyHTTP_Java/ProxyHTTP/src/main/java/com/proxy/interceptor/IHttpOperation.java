package com.proxy.interceptor;

import java.util.List;

import com.proxy.interceptor.httpOperation.request.Header;

/** Interfaz que define los métodos comunes de las operaciones HTTP.
 * Las operaciones HTTP son dos: peticiones y respuestas.
 * @author Pablo
 *
 */
public interface IHttpOperation {
	/** Devuelve el Host de una operación HTTP.
	 * @return Host de una operación HTTP.
	 */
	String getHost();

	/** Establece el Host de una operación HTTP.
	 * @param host Host a establecer.
	 */
	void setHost(String host);

	/** Devuelve (si existe) una cabecera de la lista de cabeceras.
	 * @param name Nombre de la cabecera a buscar.
	 * @return Header.
	 */
	Header getHeader(String name);

	/** Devuelve la lista de cabeceras de una operación HTTP.
	 * @return Lista de cabeceras.
	 */
	List<Header> getHeaders();
	
	/** Establece el nuevo valor de una cabecera.
	 * @param headerName Nombre de la cabecera.
	 * @param newValue Nuevo valor a establecer a la cabecera.
	 */
	void setHeader(String headerName, String newValue);

	/** Obtiene el cuerpo de una operación HTTP.
	 * @return bytes del cuerpo.
	 */
	byte[] getBody();

	/** Establece el cuerpo de una operación HTTP.
	 * @param body Cuerpo de una operación HTTP.
	 */
	void setBody(byte[] body);
	
	/** Añade un nuevo mensaje de error a mostrar al usuario
	 * antes de que éste pueda añadir una excepción de seguridad.
	 * @param message Mensaje a mostrar al usuario.
	 */
	void addErrorMessage(String message);
	
	/** Devuelve todos los mensajes de error almacenados.
	 * @return Mensajes de error.
	 */
	String getAllMessages();
}
