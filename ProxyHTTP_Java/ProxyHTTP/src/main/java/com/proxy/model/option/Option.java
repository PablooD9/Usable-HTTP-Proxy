package com.proxy.model.option;

/** Interfaz que define las operaciones propias de una Opción.
 * @author Pablo
 *
 */
public interface Option {
	
	/** Devuelve el nombre de una opción.
	 * @return Nombre de la opción.
	 */
	public String getOptName();
	
	/** Establece el nombre de una opción.
	 * @param optName Nombre de una opción.
	 */
	public void setOptName(String optName);
	
	/** Devuelve True si una opción está bien construida, False en otro caso.
	 * @return True si una opción está bien construida, False en otro caso.
	 */
	public boolean parse();
	
	/** Método que devuelve un tipo de Option dependiendo del tipo de opción
	 * pasado por parámetro.
	 * @param line Línea a partir de la cual se obtiene toda la información sobre una opción.
	 * @param optionType Tipo de opción.
	 * @return Opción.
	 */
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
