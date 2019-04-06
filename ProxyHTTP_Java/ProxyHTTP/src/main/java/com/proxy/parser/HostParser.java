package com.proxy.parser;


/** Clase encargada de eliminar las cadenas de caracteres que no son necesarias de la lista
 * de hosts maliciosos.
 * @author Pablo
 *
 */
public class HostParser {
	
	public String parse(String line) {
		if (!line.startsWith("#") && line.length() > 0) {
			String host = line.split(" ")[1];
			return host;
		}
		
		return null;
	}
}
