package com.proxy.model.option;

/** Clase abstracta que implementa las operaciones de la interfaz Option.
 * @author Pablo
 *
 */
public abstract class AbstractOption implements Option {

	private String optName;
	
	public AbstractOption(String optName) {
		setOptName(optName);
	}

	@Override
	public String getOptName() {
		return optName;
	}

	@Override
	public void setOptName(String optName) {
		this.optName = optName;
	}
	
	@Override
	public boolean parse() {
		if (optName.trim().startsWith("#") || optName.trim().length() == 0) {
			return false;
		}
		
		return true;
	}
	
}
