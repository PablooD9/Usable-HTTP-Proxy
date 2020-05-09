package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

public class CheckSpamHost extends CheckHost {

	public CheckSpamHost(IProxyFunctionality functionality) {
		super(functionality);
	}
	
	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getCheckIfTrackersHosts().equalsIgnoreCase("true");
	}

}
