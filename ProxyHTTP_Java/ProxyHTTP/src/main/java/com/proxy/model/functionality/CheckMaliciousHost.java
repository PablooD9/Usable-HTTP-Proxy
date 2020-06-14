package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear hosts maliciosos está activa.
 * @author Pablo
 *
 */
public class CheckMaliciousHost extends CheckProxyFunctionality {

	public CheckMaliciousHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getCheckIfMaliciousHosts().equalsIgnoreCase("true");
	}

}
