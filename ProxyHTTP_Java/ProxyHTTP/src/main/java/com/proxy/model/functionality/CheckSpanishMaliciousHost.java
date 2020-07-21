package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear hosts españoles potencialmente peligrosos está activa.
 * @author Pablo
 *
 */
public class CheckSpanishMaliciousHost extends CheckProxyFunctionality {

	public CheckSpanishMaliciousHost(){}
	
	public CheckSpanishMaliciousHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	public boolean isAnOptionActive() {
		if (UserConfiguration.getInstance().getConfiguration().getCheckIfSpanishMaliciousHosts() != null) {
			return UserConfiguration.getInstance().getConfiguration().getCheckIfSpanishMaliciousHosts().equalsIgnoreCase("true");
		}
		else {
			return false;
		}
	}

}
