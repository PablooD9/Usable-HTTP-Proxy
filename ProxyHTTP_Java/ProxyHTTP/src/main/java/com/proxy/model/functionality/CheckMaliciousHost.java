package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

public class CheckMaliciousHost extends CheckHost {

	public CheckMaliciousHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getOp3().equalsIgnoreCase("true");
	}

}
