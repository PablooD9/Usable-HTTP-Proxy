package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

public class CheckTrackerHost extends CheckHost {

	public CheckTrackerHost(IProxyFunctionality functionality) {
		super(functionality);
	}
	
	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getOp4().equalsIgnoreCase("true");
	}

}
