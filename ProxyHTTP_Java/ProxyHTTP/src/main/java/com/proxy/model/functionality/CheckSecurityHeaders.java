package com.proxy.model.functionality;

public class CheckSecurityHeaders extends CheckHost {

	public CheckSecurityHeaders(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		return false;
	}

}
