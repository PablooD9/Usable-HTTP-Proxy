package com.proxy.model.functionality;

import java.util.ArrayList;
import java.util.List;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;
import com.proxy.model.hosttype.Host;

/** Clase abstracta que implementa las operaciones necesarias para comprobar si un
 * Host se encuentra dentro de la lista de hosts maliciosos a escanear.
 * @author Pablo
 *
 */
public abstract class CheckProxyFunctionality extends AbstractProxyFunctionality {
	
	public CheckProxyFunctionality() {}
	
	public CheckProxyFunctionality(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (hostIsInHostsDangerousList(operation.getHost()))
			return null;

		return getFunctionality().modify(operation);
	}

	/** Comprueba si un Host pasado por parámetro se encuentra en la lista de hosts a escanear.
	 * @param hostToFind Host que se comprobará si está en la lista de hosts maliciosos.
	 * @return True si el Host está en la lista, False en otro caso.
	 */
	private boolean hostIsInHostsDangerousList(String hostToFind) {
		List<Host> hostsToScan = new ArrayList<Host>();
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive()) {
			hostsToScan = loadHostsList();
		}
		if (hostsToScan == null || hostsToScan.isEmpty()) {
			return false;
		}
		Host hostFound = hostsToScan.stream().filter(host -> hostToFind.contains(host.getHostName())).findFirst()
				.orElse(null);

		if (hostFound == null) { // Host not found (that is good)
			return false;
		}
		return true;
	}

	/** Método que devuelve una lista con los hosts potencialmente peligrosos.
	 * @return Lista de Host.
	 */
	private List<Host> loadHostsList() {
		return UserConfiguration.getInstance().getMaliciousHostsToScan();
	}

	/** Método que comprueba si una determinada opción ha sido activada en la pantalla de configuración
	 * por parte del usuario. Será implementado por las subclases.
	 * @return True si la opción está activa, False en otro caso.
	 */
	public abstract boolean isAnOptionActive();
}
