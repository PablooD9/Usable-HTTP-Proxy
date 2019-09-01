package com.proxy.model.option;

import java.util.ArrayList;
import java.util.List;

public class DefaultOptionOS extends AbstractDefaultOption {

	public DefaultOptionOS(String filePath) {
		super(filePath);
	}

	@Override
	List<Option> loadOptions() {
		List<Option> options = new ArrayList<>();
		
		options.add( new OptionImpl("Windows") );
		options.add( new OptionImpl("Android") );
		
		return options;
	}

}
