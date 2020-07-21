package com.proxy.model.hosttype;

/** Clase que define la URL de los hosts españoles maliciosos.
 * @author Pablo
 *
 */
public class SpanishMaliciousHost extends Host {
	
	private final static String URL_spa_malicious_hosts =
			"https://raw.githubusercontent.com/rafamerino/hosts-ads-spain/master/hosts.txt";
	
	public SpanishMaliciousHost() {}

	@Override
	public String getURLOfHostList() {
		return URL_spa_malicious_hosts;
	}
	
}
