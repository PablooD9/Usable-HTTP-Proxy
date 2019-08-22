package com.proxy.model.hosttype;

public class CreateHost implements CreateHostFactory {

	@Override
	public Host createHost(HostType hostType) {

		if (hostType.equals( HostType.Malicious_Hosts ))
			return new MaliciousHostType();
		else if (hostType.equals( HostType.Trackers_Hosts ))
			return new TrackerHostType();
		else if (hostType.equals( HostType.Spam_Hosts ))
			return new SpamHostType();
		else if (hostType.equals( HostType.Pornography_Hosts ))
			return new PornographyHostType();
		
		throw new IllegalStateException("Class not implemented yet!");
	}

}
