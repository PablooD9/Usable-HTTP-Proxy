package com.proxy.model.hosttype;

/** Clase que define la URL de los hosts que rastrean a los usuarios.
 * @author Pablo
 *
 */
public class TrackerHost extends Host {

	private final static String URL_trackers_hosts =
			"https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&mimetype=plaintext&useip=0.0.0.0";
	
	public TrackerHost() {}

	@Override
	public String getURLOfHostList() {
		return URL_trackers_hosts;
	}

}
