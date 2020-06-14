package com.proxy.model.hosttype;

/** Enumerable con todos los tipos de hosts de la aplicación.
 * @author Pablo
 *
 */
public enum HostType {

	Malicious_Hosts,
	Trackers_Hosts,
	Spanish_Malicious_Hosts,
	Pornography_Hosts;
	
	public static int getSize() {
		return HostType.values().length;
	}
}
