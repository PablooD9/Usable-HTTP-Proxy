package com.proxy.parser;

import java.util.ArrayList;
import java.util.List;

/** Clase encargada de eliminar las cadenas de caracteres que no son necesarias de la lista
 * de hosts maliciosos.
 * @author Pablo
 *
 */
public class HostParser {
	private List<String> hostList;
	
	public HostParser(List<String> hostList) {
		this.hostList = hostList;
	}
	
	public List<String> parse() {
		List<String> parsedHostList = new ArrayList<>();
		for (String line : hostList) {
			if (!line.startsWith("#") && line.length() > 0) {
				String host = line.split(" ")[1];
				parsedHostList.add( host );
			}
		}
		
		return parsedHostList;
	}
}
