package com.proxy.model.hosttype;

public enum HostType {

	Malicious_Hosts,
	Trackers_Hosts,
	Spam_Hosts,
	Pornography_Hosts;
	
	public static int getSize() {
		return HostType.values().length;
	}
}
