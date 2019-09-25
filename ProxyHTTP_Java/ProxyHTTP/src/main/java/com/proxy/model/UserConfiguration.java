package com.proxy.model;

import java.util.ArrayList;
import java.util.List;

import com.proxy.model.hosttype.Host;

public class UserConfiguration 
{
	private final static UserConfiguration USER_CONFIGURATION = new UserConfiguration();
	private Configuration configuration;
	private List<Host> maliciousHostsToScan = new ArrayList<>();
	
	private UserConfiguration() {}

	public static UserConfiguration getInstance() {
		return USER_CONFIGURATION;
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public List<Host> getMaliciousHostsToScan() {
		return maliciousHostsToScan;
	}
	public void setMaliciousHostsToScan(List<Host> maliciousHostsToScan) {
		this.maliciousHostsToScan = maliciousHostsToScan;
	}
	
	public boolean hostIsInList(String hostToFind) {
		Host hostFound = maliciousHostsToScan.parallelStream().filter(host -> host.getHostName().equalsIgnoreCase( hostToFind ))
				 .findFirst()
				 .orElse(null);
		
		if (hostFound == null)
			return false;
		return true;
	}
}
