package com.proxy.model.functionality;

import com.proxy.model.hosttype.HostType;

public class CheckPornographyHost extends CheckHost {

	public CheckPornographyHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	void loadHostsList() {
		setHostsList( getHostService().getHostsFromType( HostType.Pornography_Hosts ) );
	}

}
