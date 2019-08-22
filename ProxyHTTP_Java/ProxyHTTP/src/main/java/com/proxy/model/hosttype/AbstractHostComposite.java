package com.proxy.model.hosttype;

import java.util.List;

public abstract class AbstractHostComposite extends Host {

	@Override
	public String getURLOfHostList() {
		// TODO
		throw new IllegalStateException("");
	}
	
	public abstract void addHost(HostType hostType);
	public abstract void updateHostsList();
	public abstract List<Host> obtainHostsList(HostType hType);
}
