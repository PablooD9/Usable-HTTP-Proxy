package com.proxy.model.option;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public abstract class AbstractDefaultOption implements DefaultOption {

	private String filePath;
	
	public AbstractDefaultOption(String filePath) {
		this.filePath = filePath;
	}
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    
		return options;
	}
	
	abstract List<Option> loadOptions();
	
}
