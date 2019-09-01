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
import com.proxy.model.UserConfiguration;
import com.proxy.model.option.DefaultOption;
import com.proxy.model.option.DefaultOptionBrowser;
import com.proxy.model.option.DefaultOptionOS;
import com.proxy.model.option.DefaultOptionUserAgent;
import com.proxy.model.option.Option;
import com.proxy.model.option.OptionUserAgent;
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
	private List<Option> browserOptions;
	
	private final static String USER_AGENT_FILE_PATH = "src/main/resources/static/otherFiles/UAConfiguration.txt";
	private List<Option> UAOptions;
	
	private final static String[] OPTION_TYPES = new String[] { "Default", "User-Agent" };
	
	private boolean configurationIsActive = false;
	
	public List<Option> getOSOptions(){ return OSOptions; }
	public List<Option> getBrowserOptions(){ return browserOptions; }
	public List<Option> getUAOptions(){ return UAOptions; }
	
	
	@PostConstruct
	private void loadOptionsInfo() {
		OSOptions = readOptionsFile(OS_FILE_PATH, OPTION_TYPES[0]);
		browserOptions = readOptionsFile(BROWSER_FILE_PATH, OPTION_TYPES[0]);
		UAOptions = readOptionsFile(USER_AGENT_FILE_PATH, OPTION_TYPES[1]);
		
		if (optionsAreNull())
			loadDefaultOptions();
	}
	
	private boolean optionsAreNull() {
		if (OSOptions == null || browserOptions == null || UAOptions == null)
			return true;
		
		return false;
	}
	
	private List<Option> readOptionsFile(String file_path, String optionType){
		List<Option> optionsList = new ArrayList<>();
		
		File file = new File(file_path);
		if (!file.exists()) {
			return null;
		}
		else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = "";
				while ((line = reader.readLine()) != null) {
					Option option = Option.parseOptionLine(line, optionType);
					option.parse();
					if (option.parse() == true) {
						optionsList.add( option );
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return optionsList;
	}
	
	public Configuration buildConfigurationObject(String op1_os, String op1_browser, String op2,
												  String op3, String op4, String op5) {
		String userEmail = userService.getEmailOfLoggedInUser();
		Configuration configuration = new Configuration(userEmail, op1_os, op1_browser, op2, op3, op4, op5);
		return configuration;
	}
	
	public void saveConfiguration(Configuration configuration) {
		if (userService.userIsLoggedIn())
			confRepository.save( configuration );
		UserConfiguration.getInstance().setConfiguration( configuration );
		configurationIsActive = true;
	}
	
	public Configuration getConfigOfUser() {
		Optional<Configuration> optionalConf = confRepository.findById( userService.getEmailOfLoggedInUser() );
		if (!optionalConf.isEmpty())
			return optionalConf.get();
		else if (configurationIsActive)
			return UserConfiguration.getInstance().getConfiguration();
			
		return null;
	}
	
	private void loadDefaultOptions(){
		DefaultOption defOSOption = new DefaultOptionOS(OS_FILE_PATH);
		OSOptions = defOSOption.getOptions();
		
		DefaultOption defBrowserOption = new DefaultOptionBrowser(BROWSER_FILE_PATH);
		browserOptions = defBrowserOption.getOptions();
		
		DefaultOption defUAOption = new DefaultOptionUserAgent(USER_AGENT_FILE_PATH);
		UAOptions = defUAOption.getOptions();
	}
	
	public String getUserAgent() {
		
		String OS = UserConfiguration.getInstance().getConfiguration().getOp1_os();
		String browser = UserConfiguration.getInstance().getConfiguration().getOp1_browser();
		
		System.err.println("Buscando " + OS + ", " + browser);
		
		for (Option option : UAOptions) {
			if (option instanceof OptionUserAgent)
			{
				String ua = ((OptionUserAgent) option).getUserAgentIfValid(OS, browser);
				if (ua != null) {
					System.err.println("AQUIIIIUFUH:: " + ua);
					return ua;
				}
			}
		}
		
		return null;
	}
}
