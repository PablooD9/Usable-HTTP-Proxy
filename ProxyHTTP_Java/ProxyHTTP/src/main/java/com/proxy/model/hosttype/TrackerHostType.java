package com.proxy.model.hosttype;

public class TrackerHostType extends Host {

	private final static String URL_trackers_hosts =
			"https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&mimetype=plaintext&useip=0.0.0.0";
	
	public TrackerHostType() {}
	
	public TrackerHostType(Integer _id, String hostName) {
		super(_id, hostName);
	}

	@Override
	public String getURLOfHostList() {
		return URL_trackers_hosts;
	}

}
