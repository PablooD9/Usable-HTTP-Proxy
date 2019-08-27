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
		System.out.println(userConfig);
		
		model.addAttribute("userLoggedIn", userLoggedIn);
		model.addAttribute("userConfig", userConfig);
		
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
						 @RequestParam("_op5") String op5) 
	{	
		
		System.out.println( op1_os + "\n" + op1_browser + "\n" + op2 + "\n" + op3 + "\n" + op4 + "\n" + op5);
		
		if (userService.getUserLoggedIn() != null) {
			Configuration configuration = confService.buildConfigurationObject(op1_os, op1_browser, op2, op3, op4, op5);
			confService.saveConfiguration(configuration);
		}
		
	    return "OK!";
	}
	
}
