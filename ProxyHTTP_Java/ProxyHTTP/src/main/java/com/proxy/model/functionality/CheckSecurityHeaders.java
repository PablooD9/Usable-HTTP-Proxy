package com.proxy.model.functionality;

import java.util.List;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear los sitios web que no implementen ciertas cabeceras
 * de seguridad en sus respuestas está activa.
 * @author Pablo
 *
 */
public class CheckSecurityHeaders extends CheckProxyFunctionality {

	public CheckSecurityHeaders(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	public boolean isAnOptionActive() {
		String headers = UserConfiguration.getInstance().getConfiguration().getSecurityHeaders();
		return (headers != null && headers.length() > 1);
	}
	
	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive()) {
			if (checkIfThereIsSecurityException( operation )) {
				return getFunctionality().modify( operation );
			}
			String preHeaders = UserConfiguration.getInstance().getConfiguration().getSecurityHeaders();
			String[] headers;
			if (preHeaders.contains(",")) {
				headers = preHeaders.split(",");
			}
			else {
				headers = new String[] { preHeaders };
			}
			for (int i=0; i< headers.length; i++) {
				if (operation.getHeader( headers[i] ) == null) {
					operation.addErrorMessage("Website does not implement the security headers set in the configuration options!");
					return null; // Host does not implement the security header in their responses.
				}
			}
		}
		
		return getFunctionality().modify( operation );
	}
	
	/** Devuelve True si existe una excepción de seguridad aplicada sobre un Host concreto.
	 * @param operation Operación HTTP a partir de la cual se obtiene el Host.
	 * @return True si existe una excepción de seguridad aplicada sobre un Host concreto, False en otro caso.
	 */
	private boolean checkIfThereIsSecurityException(IHttpOperation operation) {
		String host = operation.getHost();
		List<String> hostExceptions = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		for (String exception : hostExceptions)
			if (exception.equalsIgnoreCase(host))
				return true;
		
		return false;
	}

}
