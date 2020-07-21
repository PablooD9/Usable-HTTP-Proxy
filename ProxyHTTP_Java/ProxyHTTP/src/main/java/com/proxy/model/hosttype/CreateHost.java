package com.proxy.model.hosttype;

public class CreateHost implements CreateHostFactory {

	@Override
	public Host createHost(HostType hostType) {

		if (hostType.equals( HostType.Malicious_Hosts ))
			return new MaliciousHost();
		else if (hostType.equals( HostType.Trackers_Hosts ))
			return new TrackerHost();
		else if (hostType.equals( HostType.Pornography_Hosts ))
			return new PornographyHost();
		else if (hostType.equals(HostType.Spanish_Malicious_Hosts))
			return new SpanishMaliciousHost();
		
		throw new IllegalStateException("Class not implemented yet!");
	}

}
