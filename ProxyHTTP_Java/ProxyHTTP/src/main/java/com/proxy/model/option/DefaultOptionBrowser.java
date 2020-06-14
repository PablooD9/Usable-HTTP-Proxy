package com.proxy.model.option;

import java.util.ArrayList;
import java.util.List;

/** Clase que representa las opciones de navegadores por defecto.
 * @author Pablo
 *
 */
public class DefaultOptionBrowser extends AbstractDefaultOption {

	public DefaultOptionBrowser(String filePath) {
		super(filePath);
	}

	@Override
	List<Option> loadOptions() {
		List<Option> options = new ArrayList<>();
		
		options.add( new OptionImpl("Google Chrome") );
		options.add( new OptionImpl("Mozilla Firefox") );
		options.add( new OptionImpl("Opera") );
		options.add( new OptionImpl("Safari") );
		options.add( new OptionImpl("Microsoft Edge") );
		
		return options;
	}
	
}
