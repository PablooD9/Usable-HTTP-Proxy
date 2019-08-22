package com.proxy.model.functionality;

import com.proxy.model.hosttype.HostType;

public class CheckSpamHost extends CheckHost {

	public CheckSpamHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	void loadHostsList() {
		setHostsList( getHostService().getHostsFromType( HostType.Spam_Hosts ) );
	}

}
