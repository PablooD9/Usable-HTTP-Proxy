package com.proxy.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proxy.model.Configuration;
import com.proxy.repository.ConfigurationRepository;

@Service
public class ConfigurationService {

	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationRepository confRepository;
	
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
