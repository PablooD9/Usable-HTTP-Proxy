package com.proxy.model.functionality;

import com.proxy.model.UserConfiguration;

public class CheckPornographyHost extends CheckHost {

	public CheckPornographyHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getOp5().equalsIgnoreCase("true");
	}

}
