package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear hosts sospechosos de rastrear a los usuarios está activa.
 * @author Pablo
 *
 */
public class CheckTrackerHost extends CheckProxyFunctionality {

	public CheckTrackerHost(IProxyFunctionality functionality) {
		super(functionality);
	}
	
	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getCheckIfTrackersHosts().equalsIgnoreCase("true");
	}

}
