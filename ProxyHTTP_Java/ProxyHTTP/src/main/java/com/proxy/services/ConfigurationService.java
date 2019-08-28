package com.proxy.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proxy.model.Configuration;
import com.proxy.model.option.Option;
import com.proxy.repository.ConfigurationRepository;

@Service
public class ConfigurationService {

	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationRepository confRepository;
	
	private final static String OS_FILE_PATH = "src/main/resources/static/otherFiles/OS.txt";
	private List<Option> OSOptions;
	
	private final static String BROWSER_FILE_PATH = "src/main/resources/static/otherFiles/Browsers.txt";
	private List<Option> BrowserOptions;
	
	@PostConstruct
	private void loadUserAgentInfo() {
		OSOptions = readOptionsFile(OS_FILE_PATH);
		BrowserOptions = readOptionsFile(BROWSER_FILE_PATH);
	}
	
	private List<Option> readOptionsFile(String file_path){
		List<Option> optionsList = new ArrayList<>();
		
		File file = new File(file_path);
		if (!file.exists())
			return null;
//			return new Default...
		else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = "";
				while ((line = reader.readLine()) != null) {
					Option option = parseOptionLine(line);
					if (option != null)
						optionsList.add( option );
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		
		return optionsList;
	}
	
	private Option parseOptionLine(String line) {
		if (line.trim().startsWith("#")) {
			return null;
		}
		else {
			return new Option(line);
		}
	}
	
	public List<Option> getOSOptions(){
		return OSOptions;
	}

	public List<Option> getBrowserOptions(){
		return BrowserOptions;
	}
	
	public Configuration buildConfigurationObject(String op1_os, String op1_browser, String op2,
												  String op3, String op4, String op5) {
		String userEmail = userService.getEmailOfLoggedInUser();
		Configuration configuration = new Configuration(userEmail, op1_os, op1_browser, op2, op3, op4, op5);
		return configuration;
	}
	
	public void saveConfiguration(Configuration configuration) {
		confRepository.save( configuration );
	}
	
	public Configuration getConfigOfUser() {
		Optional<Configuration> optionalConf = confRepository.findById( userService.getEmailOfLoggedInUser() );
		if (!optionalConf.isEmpty())
			return optionalConf.get();
		return null;
	}
}
