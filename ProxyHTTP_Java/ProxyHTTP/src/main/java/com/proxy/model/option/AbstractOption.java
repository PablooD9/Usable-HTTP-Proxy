package com.proxy.model.option;

public abstract class AbstractOption implements Option {

	private String optName;
	
	public AbstractOption(String optName) {
		setOptName(optName);
	}

	public String getOptName() {
		return optName;
	}

	public void setOptName(String optName) {
		this.optName = optName;
	}
	
	public boolean parse() {
		if (optName.trim().startsWith("#") || optName.length() == 0) {
			return false;
		}
		
		return true;
	}
	
}
