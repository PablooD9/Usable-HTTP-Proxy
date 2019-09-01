package com.proxy.model.option;

public interface Option {
	
	public String getOptName();
	public void setOptName(String optName);
	public boolean parse();
	
	public static Option parseOptionLine(String line, String optionType) {
		if (optionType.equalsIgnoreCase( "Default" )){
			return new OptionImpl( line );
		}
		else if (optionType.equalsIgnoreCase( "User-Agent" )) {
			return new OptionUserAgent( line );
		}
		
		throw new IllegalStateException("Option type not implemented yet!");
	}
	
}
