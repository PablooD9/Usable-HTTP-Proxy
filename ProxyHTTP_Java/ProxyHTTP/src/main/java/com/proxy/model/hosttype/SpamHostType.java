package com.proxy.model.hosttype;

public class SpamHostType extends Host {

	private final static String URL_spam_hosts =
			"https://raw.githubusercontent.com/FadeMind/hosts.extras/master/add.Spam/hosts";
	
	public SpamHostType() {}
	
	public SpamHostType(Integer _id, String hostName) {
		super(_id, hostName);
	}
	
	@Override
	public String getURLOfHostList() {
		// TODO Auto-generated method stub
		return URL_spam_hosts;
	}

}
