package com.proxy.model.option;

/** Clase que representa una opción relacionada con la cabecera User-Agent.
 * @author Pablo
 *
 */
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

	/** A partir de un sistema operativo y un navegador pasados por parámetro, 
	 * se comprueba si coinciden con el sistema operativo y el navegador establecidos
	 * en esta clase.
	 * @param OS Sistema operativo.
	 * @param browser Navegador.
	 * @return Cabecera User-Agent si los valores coinciden, Null en otro caso.
	 */
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
	
	/** Obtiene el sistema operativo.
	 * @return Sistema operativo.
	 */
	private String getOS() {
		String[] parts = getOptName().split("\\$\\$");
		String OS = parts[0].trim();
		return OS;
	}

	/** Obtiene el navegador.
	 * @return Navegador.
	 */
	private String getBrowser() {
		String[] parts = getOptName().split("\\$\\$");
		String browser = parts[1].trim().split(">>")[0];
		return browser;
	}
	
	/** Obtiene el valor de la cabecera User-Agent.
	 * @return Cabecera User-Agent.
	 */
	private String getUAHeader() {
		String[] parts = getOptName().split(">>");
		String uaHeader = parts[1].trim();
		return uaHeader;
	}

}
