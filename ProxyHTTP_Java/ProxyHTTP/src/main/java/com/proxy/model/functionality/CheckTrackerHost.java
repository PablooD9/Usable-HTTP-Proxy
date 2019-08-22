package com.proxy.model.functionality;

import com.proxy.model.hosttype.HostType;

public class CheckTrackerHost extends CheckHost {

	public CheckTrackerHost(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	void loadHostsList() {
		setHostsList( getHostService().getHostsFromType( HostType.Trackers_Hosts ) );
	}

}
