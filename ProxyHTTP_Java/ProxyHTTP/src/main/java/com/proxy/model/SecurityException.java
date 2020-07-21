package com.proxy.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Clase que modela una Excepción de Seguridad dentro de la aplicación.
 * Cada Excepción de Seguridad contiene el Email del usuario que la ha aplicado,
 * y la lista de hosts sobre los que se aplican estas excepciones.
 * @author Pablo
 *
 */
@Document(collection = "Security_Exception")
public class SecurityException {

	@Id
	private String email;
	private String hostsException;
	
	public SecurityException() {}
	
	public SecurityException(String email, String hostsException) {
		setEmail(email);
		setHostsException( hostsException );
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getHostsException() {
		return hostsException;
	}
	public void setHostsException(String hostsException) {
		this.hostsException = hostsException;
	}
	
}
