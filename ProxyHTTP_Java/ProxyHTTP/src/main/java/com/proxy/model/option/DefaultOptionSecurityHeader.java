package com.proxy.model.option;

import java.util.ArrayList;
import java.util.List;

public class DefaultOptionSecurityHeader extends AbstractDefaultOption {

	public DefaultOptionSecurityHeader(String filePath) {
		super(filePath);
	}

	@Override
	List<Option> loadOptions() {
		List<Option> options = new ArrayList<>();
		
		options.add( new OptionImpl("Content-Security-Policy") );
		options.add( new OptionImpl("Strict-Transport-Security") );
		options.add( new OptionImpl("X-Content-Type-Options") );
		options.add( new OptionImpl("X-Frame-Options") );
		options.add( new OptionImpl("X-Xss-Protection") );
		
		return options;
	}

}
