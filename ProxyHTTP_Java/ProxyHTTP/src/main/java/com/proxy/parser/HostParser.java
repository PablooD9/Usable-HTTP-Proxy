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
		whiteHostList.add("localhost");
		whiteHostList.add("localhost.localdomain");
		whiteHostList.add("local");
		whiteHostList.add("broadcasthost");
		whiteHostList.add("ip6-localhost");
		whiteHostList.add("ip6-loopback");
		whiteHostList.add("ip6-localnet");
		whiteHostList.add("ip6-mcastprefix");
		whiteHostList.add("ip6-allnodes");
		whiteHostList.add("ip6-allrouters");
		whiteHostList.add("ip6-allhosts");
		whiteHostList.add("0.0.0.0");
	}
	
	public String parse(String line) {
		if (isValid( line )) {
			String host = line.split("[ ]+")[1]; // Split in 1 or more white spaces.
			return host;
		}
		return null;
	}
	
	private boolean isValid(String line) {
		if (!line.startsWith("#")
				&& !line.equals("")
				&& line.length() > 0
				&& !whiteHostList.contains(new String(line.split("[ ]+")[1])))
			return true;
		return false;
	}
}
