package com.proxy.model.hosttype;

/** Clase que define la URL de los hosts con contenido pornográfico.
 * @author Pablo
 *
 */
public class PornographyHost extends Host {

	private final static String URL_pornographic_hosts =
			"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/porn/hosts";
	
	public PornographyHost() {} 

	@Override
	public String getURLOfHostList() {
		return URL_pornographic_hosts;
	}

}
