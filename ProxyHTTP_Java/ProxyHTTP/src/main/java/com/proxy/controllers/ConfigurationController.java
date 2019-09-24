package com.proxy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proxy.model.Configuration;
import com.proxy.model.User;
import com.proxy.services.ConfigurationService;
import com.proxy.services.HostService;
import com.proxy.services.LanguageService;
import com.proxy.services.UserService;

@Controller
public class ConfigurationController {
	
	@Autowired
	private HostService hostService;
	@Autowired
	private UserService userService;
	@Autowired
	private LanguageService langService;
	@Autowired
	private ConfigurationService confService;
	
	@RequestMapping(value={"", "/", "/configuration"})
	public String getConfiguration(@RequestParam(required = false, name = "lang") String language, Model model) {
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		
		User userLoggedIn = userService.getUserLoggedIn();
		Configuration userConfig = confService.getConfigOfUser();
		
		model.addAttribute("userLoggedIn", userLoggedIn);
		model.addAttribute("userConfig", userConfig);
		model.addAttribute("OSOptions", confService.getOSOptions());
		model.addAttribute("BrowserOptions", confService.getBrowserOptions());
		model.addAttribute("securityHeaders", confService.getSecurityHeaders());
		model.addAttribute("hostExceptions", confService.getExceptionsList( null ));
		
		return "configuration/configuration";
	}
	
	@RequestMapping("/updateMaliciousHosts")
	public String updateMaliciousHosts() {
		hostService.updateHostsList();
		return "redirect:/";
	}
	
	@RequestMapping(value = "/savePreferences", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String savePreferences(@RequestParam("_op1_os") String op1_os, 
						 @RequestParam("_op1_browser") String op1_browser,
						 @RequestParam("_op2") String op2,
						 @RequestParam("_op3") String op3,
						 @RequestParam("_op4") String op4,
						 @RequestParam("_op5") String op5,
						 @RequestParam("_op6") String op6) 
	{	
		Configuration configuration = confService.buildConfigurationObject(op1_os, op1_browser, op2, op3, op4, op5, op6);
		confService.saveConfiguration(configuration);
		
	    return "";
	}
	
	@RequestMapping("/addSecurityException")
	public String addSecurityException(@RequestParam("host") String host, Model model) {
		boolean exceptionAdded = confService.saveSecurityException(host);
		
		model.addAttribute("lang", langService.getActualLocaleLang( null ));
		model.addAttribute("host", host);
		model.addAttribute("securityExceptionExists", exceptionAdded);
		
		return "configuration/addSecurityException";
	}
	
	@RequestMapping(value = "/deleteSecurityException", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String deleteSecurityException(@RequestParam("host") String host) {
		confService.deleteSecurityException(host);
		
		return "";
	}
	
}
