package com.proxy.model.hosttype;

/** Clase que define la URL de los hosts maliciosos.
 * @author Pablo
 *
 */
public class MaliciousHost extends Host { 
	
	private final static String URL_malicious_hosts =
			/*"https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"*/
			"https://www.malwaredomainlist.com/hostslist/hosts.txt";
	
	public MaliciousHost() {}

	@Override
	public String getURLOfHostList() {
		return URL_malicious_hosts;
	}
	
}
