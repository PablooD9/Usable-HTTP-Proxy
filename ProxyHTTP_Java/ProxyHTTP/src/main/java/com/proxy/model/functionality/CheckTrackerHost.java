package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

/** Clase que comprueba si la opción para bloquear hosts sospechosos de rastrear a los usuarios está activa.
 * @author Pablo
 *
 */
public class CheckTrackerHost extends CheckProxyFunctionality {

	public CheckTrackerHost(){}
	
	public CheckTrackerHost(IProxyFunctionality functionality) {
		super(functionality);
	}
	
	@Override
	public boolean isAnOptionActive() {
		if (UserConfiguration.getInstance().getConfiguration().getCheckIfTrackersHosts() != null) {
			return UserConfiguration.getInstance().getConfiguration().getCheckIfTrackersHosts().equalsIgnoreCase("true");
		}
		else {
			return false;
		}
	}

}
