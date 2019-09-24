package com.proxy.model.option;

public class OptionUserAgent extends AbstractOption {
	
	public OptionUserAgent(String line) {
		super(line);
	}

	@Override
	public boolean parse() {
		if (getOptName().trim().startsWith("#") || getOptName().length() == 0)
			return false;
		
		if (!getOptName().contains("$$") && !getOptName().contains(">>"))
			return false;
		
		return true;
	}

	public String getUserAgentIfValid(String OS, String browser) {
		String uaOS = getOS().trim();
		String uaBrowser = getBrowser().trim();
		String uaHeader = getUAHeader().trim();
		
		if (!uaOS.equalsIgnoreCase( OS.trim() ) 
			|| !uaBrowser.equalsIgnoreCase( browser.trim() ))
			return null;
		else
			return uaHeader;
			
	}
	
	private String getOS() {
		String[] parts = getOptName().split("\\$\\$");
		String OS = parts[0].trim();
		return OS;
	}

	private String getBrowser() {
		String[] parts = getOptName().split("\\$\\$");
		String browser = parts[1].trim().split(">>")[0];
		return browser;
	}
	
	private String getUAHeader() {
		String[] parts = getOptName().split(">>");
		String uaHeader = parts[1].trim();
		return uaHeader;
	}

}
