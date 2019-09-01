package com.proxy.model;

public class UserConfiguration 
{
	private final static UserConfiguration USER_CONFIGURATION = new UserConfiguration();
	private Configuration configuration;
	
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

}
