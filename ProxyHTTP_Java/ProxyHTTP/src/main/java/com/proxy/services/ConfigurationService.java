package com.proxy.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proxy.model.Configuration;
import com.proxy.model.SecurityException;
import com.proxy.model.UserConfiguration;
import com.proxy.model.option.DefaultOption;
import com.proxy.model.option.DefaultOptionBrowser;
import com.proxy.model.option.DefaultOptionOS;
import com.proxy.model.option.DefaultOptionSecurityHeader;
import com.proxy.model.option.DefaultOptionUserAgent;
import com.proxy.model.option.Option;
import com.proxy.model.option.OptionUserAgent;
import com.proxy.repository.ConfigurationRepository;
import com.proxy.repository.SecurityExceptionRepository;

@Service
public class ConfigurationService {

	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationRepository confRepository;
	@Autowired
	private SecurityExceptionRepository secExceptionRepository;
	
	private final static String OS_FILE_PATH = "src/main/resources/static/otherFiles/OS.txt";
	private List<Option> OSOptions;
	
	private final static String BROWSER_FILE_PATH = "src/main/resources/static/otherFiles/Browsers.txt";
	private List<Option> browserOptions;
	
	private final static String SECURITY_HEADERS_FILE_PATH = "src/main/resources/static/otherFiles/SecurityHeaders.txt";
	private List<Option> securityHeaders;
	
	private final static String USER_AGENT_FILE_PATH = "src/main/resources/static/otherFiles/UAConfiguration.txt";
	private List<Option> UAOptions;
	
	private final static String[] OPTION_TYPES = new String[] { "Default", "User-Agent" };
	
	private boolean configurationIsActive = false;
	
	public List<Option> getOSOptions(){ return OSOptions; }
	public List<Option> getBrowserOptions(){ return browserOptions; }
	public List<Option> getUAOptions(){ return UAOptions; }
	public List<Option> getSecurityHeaders(){ return securityHeaders; }
	
	@PostConstruct
	private void loadOptionsInfo() {
		OSOptions = readOptionsFile(OS_FILE_PATH, OPTION_TYPES[0]);
		browserOptions = readOptionsFile(BROWSER_FILE_PATH, OPTION_TYPES[0]);
		securityHeaders = readOptionsFile(SECURITY_HEADERS_FILE_PATH, OPTION_TYPES[0]);
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
												  String op3, String op4, String op5, String op6) {
		String userEmail = userService.getEmailOfLoggedInUser();
		Configuration configuration = new Configuration(userEmail, op1_os, op1_browser, op2, op3, op4, op5, op6);
		return configuration;
	}
	
	public boolean saveSecurityException(String host) {
		String email = userService.getEmailOfLoggedInUser();
		SecurityException secException = secExceptionRepository.findByEmail( email );
		List<String> secExceptionList = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		if (secException == null) {
			secException = new SecurityException(email, host);
			if (exceptionIsInExceptionsList(secExceptionList, host))
				return false;
			secExceptionList.add(host);
			if (userService.userIsLoggedIn())
				secExceptionRepository.save( secException );
		}
		else {
			if (secException.getHostsException().toLowerCase().contains(host)) // If host is already added to the exceptions, we don't add it again
				return false;
			secExceptionList.add(host);
			updateDatabaseHostsExceptionOfUser( secExceptionList, secException );
		}
		
		return true; // We have added the host exception
	}
	
	public void deleteSecurityException(String host) {
		List<String> secExceptionList = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		if (exceptionIsInExceptionsList(secExceptionList, host)) { // If host is in user configuration list, we delete it 
			secExceptionList = deleteSecurityExceptionFromList(secExceptionList, host);
			UserConfiguration.getInstance().getConfiguration().setHostExceptions(secExceptionList);
			
			if (userService.userIsLoggedIn()) { // If user is logged in, we must delete this security exception from DB
				SecurityException secException = secExceptionRepository.findByEmail( userService.getEmailOfLoggedInUser() );
				if (secException != null) {
					updateDatabaseHostsExceptionOfUser(secExceptionList, secException);
				}
			}
		}
	}
	
	private boolean exceptionIsInExceptionsList(List<String> secExceptionList, String host) {
		if (secExceptionList == null)
			return true;
		for (String hostException : secExceptionList) {
			if (hostException.toLowerCase().equalsIgnoreCase(host))
				return true; // If host is already added to the exceptions, we don't add it again
		}
		
		return false;
	}
	
	private List<String> deleteSecurityExceptionFromList(List<String> secExceptionList, String host) {
		List<String> secExceptionLinkedList = new LinkedList<>();
		secExceptionLinkedList.addAll(secExceptionList);
		
		int counter=0;
		for (String hostException : secExceptionList) {
			if (hostException.toLowerCase().equalsIgnoreCase(host)) {
				secExceptionLinkedList.remove(counter);
			}
			counter++;
		}
		
		secExceptionList = new ArrayList<>();
		secExceptionList.addAll( secExceptionLinkedList );
		
		return secExceptionList;
	}
	
	private void updateDatabaseHostsExceptionOfUser( List<String> seList, SecurityException se) {
		String hostsException = "";
		int counter=0;
		for (String sException : seList) {
			if (counter++ == seList.size()-1)
				hostsException += sException;
			else
				hostsException += sException + ",";
		}
		se.setHostsException(hostsException);
		if (seList.isEmpty())
			secExceptionRepository.delete( se );
		else
			secExceptionRepository.save( se );
	}
	
	public void saveConfiguration(Configuration configuration) {
		UserConfiguration.getInstance().setConfiguration( configuration );
		String ua = getUserAgent(configuration);
		configuration.setUserAgent( ua );
		if (userService.userIsLoggedIn())
			confRepository.save( configuration );
		configurationIsActive = true;
	}
	
	public Configuration getConfigOfUser() {
		Optional<Configuration> optionalConf = confRepository.findById( userService.getEmailOfLoggedInUser() );
		SecurityException secException = secExceptionRepository.findByEmail( userService.getEmailOfLoggedInUser() );
		List<String> secExceptionList = new ArrayList<>();
		if (secException != null)
			secExceptionList = getExceptionsList( secException );
		if (!optionalConf.isEmpty()) {
			UserConfiguration.getInstance().setConfiguration(optionalConf.get());
			UserConfiguration.getInstance().getConfiguration().setHostExceptions(secExceptionList);
			return optionalConf.get();
		}
		else if (configurationIsActive) // Anonymous user
			return UserConfiguration.getInstance().getConfiguration();
			
		return null;
	}
	
	private void loadDefaultOptions(){
		DefaultOption defOSOption = new DefaultOptionOS(OS_FILE_PATH);
		OSOptions = defOSOption.getOptions();
		
		DefaultOption defBrowserOption = new DefaultOptionBrowser(BROWSER_FILE_PATH);
		browserOptions = defBrowserOption.getOptions();
		
		DefaultOption defSecurityHeaders = new DefaultOptionSecurityHeader(SECURITY_HEADERS_FILE_PATH);
		securityHeaders = defSecurityHeaders.getOptions();
		
		DefaultOption defUAOption = new DefaultOptionUserAgent(USER_AGENT_FILE_PATH);
		UAOptions = defUAOption.getOptions();
	}
	
	private String getUserAgent(Configuration configuration) {
		
		String OS, browser;
		if (configuration != null) {
			OS = configuration.getOS();
			browser = configuration.getBrowser();
		}
		else{
			OS = UserConfiguration.getInstance().getConfiguration().getOS();
			browser = UserConfiguration.getInstance().getConfiguration().getBrowser();
		}
		
		for (Option option : UAOptions) {
			if (option instanceof OptionUserAgent)
			{
				String ua = ((OptionUserAgent) option).getUserAgentIfValid(OS, browser);
				if (ua != null) {
					return ua;
				}
			}
		}
		
		return null;
	}
	
	public List<String> getExceptionsList(SecurityException se){
		if (se == null) {
			if (UserConfiguration.getInstance().getConfiguration() == null)
				return new ArrayList<>();
			
			return UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		}
		String[] exceptions;
		if (se.getHostsException().contains(","))
			exceptions = se.getHostsException().split(",");
		else
			exceptions = new String[] { se.getHostsException() };
		
		List<String> list = new ArrayList<>();
		for (int i=0; i< exceptions.length; i++)
			list.add(exceptions[i]);
		
		return list;
	}
}
