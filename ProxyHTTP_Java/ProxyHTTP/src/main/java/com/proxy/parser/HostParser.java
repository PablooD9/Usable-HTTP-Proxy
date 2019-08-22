package com.proxy.parser;

import java.util.ArrayList;
import java.util.List;

/** Clase encargada de eliminar las cadenas de caracteres que no son necesarias de la lista
 * de hosts maliciosos.
 * @author Pablo
 *
 */
public class HostParser {
	
	private List<String> whiteHostList = new ArrayList<>();
	
	public HostParser() {
		loadWhiteList();
	}
	
	private void loadWhiteList() {
		whiteHostList.add("127.0.0.1");
		whiteHostList.add("::1");
		whiteHostList.add("255.255.255.255");
		whiteHostList.add("fe80::1%lo0");
		whiteHostList.add("ff00::0");
		whiteHostList.add("ff02::1");
		whiteHostList.add("ff02::2");
		whiteHostList.add("ff02::3");
	}
	
	public String parse(String line) {
		if (isValid( line )) {
			String host = line.split(" ")[1];
			return host;
		}
		return null;
	}
	
	private boolean isValid(String line) {
		if (!line.startsWith("#") 
				&& line.length() > 0
				&& !whiteHostList.contains(new String(line.split(" ")[0])))
			return true;
		return false;
	}
}
