package com.proxy.model.functionality;

import com.proxy.model.hosttype.HostType;

public class CheckMaliciousHost extends CheckHost {

	public CheckMaliciousHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	void loadHostsList() {
		setHostsList( getHostService().getHostsFromType( HostType.Malicious_Hosts ) );
	}

}
