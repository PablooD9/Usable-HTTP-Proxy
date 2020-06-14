package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear hosts con contenido pornográfico está activa.
 * @author Pablo
 *
 */
public class CheckPornographyHost extends CheckProxyFunctionality {

	public CheckPornographyHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getCheckIfPornographicHosts().equalsIgnoreCase("true");
	}

}
