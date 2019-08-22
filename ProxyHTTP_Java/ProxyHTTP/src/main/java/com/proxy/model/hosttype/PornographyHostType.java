package com.proxy.model.hosttype;

public class PornographyHostType extends Host {

	private final static String URL_pornographic_hosts =
			"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/porn/hosts";
	
	public PornographyHostType() {
		// TODO Auto-generated constructor stub
	} 
	
	public PornographyHostType(Integer _id, String hostName) {
		super(_id, hostName);
	}

	@Override
	public String getURLOfHostList() {
		return URL_pornographic_hosts;
	}

}
