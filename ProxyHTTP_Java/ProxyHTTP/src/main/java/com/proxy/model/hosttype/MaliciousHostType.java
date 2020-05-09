package com.proxy.model.hosttype;

/** Malicious Hosts POJO Class
 * @author Pablo
 *
 */
public class MaliciousHostType extends Host { 
	
	private final static String URL_malicious_hosts =
			"https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts";
	
	public MaliciousHostType() {}

	@Override
	public String getURLOfHostList() {
		return URL_malicious_hosts;
	}
	
}
