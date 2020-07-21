package com.proxy.model.option;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

/** Clase abstracta que implementa las operaciones de DefaultOption.
 * @author Pablo
 *
 */
public abstract class AbstractDefaultOption implements DefaultOption {

	private String filePath;
	
	private final static Logger LOG = Logger.getLogger(AbstractDefaultOption.class);
	
	public AbstractDefaultOption(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public List<Option> getOptions(){
		BufferedWriter writer = null;
		List<Option> options = null;
		try {
			writer = new BufferedWriter(new FileWriter(filePath));
			options = loadOptions();
			int counter = 0;
			for (Option option : options) {
				if (counter++ == options.size()-1) // last option
					writer.write(option.getOptName());
				else
					writer.write(option.getOptName() + "\r\n");
			}
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}
	    
		return options;
	}
	
	/** Método abstracto a implementar por las subclases que devuelve una lista 
	 * de opciones por defecto.
	 * @return Lista de opciones.
	 */
	abstract List<Option> loadOptions();
	
}
