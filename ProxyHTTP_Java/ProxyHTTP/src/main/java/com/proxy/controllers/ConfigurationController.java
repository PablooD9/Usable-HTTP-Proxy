package com.proxy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.proxy.model.user.User;
import com.proxy.services.HostService;
import com.proxy.services.LanguageService;
import com.proxy.services.SecurityService;
import com.proxy.services.UserService;
import com.proxy.validator.SignUpValidator;

@Controller
public class ConfigurationController {
	
	@Autowired
	private HostService hostService;
	@Autowired
	private UserService userService;
	@Autowired
	private SecurityService secService;
	@Autowired
	private SignUpValidator signupValidator;
	@Autowired
	private LanguageService langService;
	
	@RequestMapping(value={"", "/", "/configuration"})
	public String getConfiguration(@RequestParam(required = false, name = "lang") String language, Model model) {
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		
		User userLoggedIn = userService.getUserLoggedIn();
		
		model.addAttribute("userLoggedIn", userLoggedIn);
		
		return "configuration/configuration";
	}
	
	@RequestMapping("/updateMaliciousHosts")
	public String updateMaliciousHosts() {
		hostService.updateHostsList();
		return "redirect:/";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(required = false, name = "lang") String language, 
						@RequestParam(required = false, name = "error") String error,
						Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		if (error != null)
			model.addAttribute("error", error);
		else
			model.addAttribute("error", "false");
		
		return "login/login";
	}
	
	@RequestMapping(value = "/login/error", method = RequestMethod.GET)
	public String loginError() {
		return "redirect:/login?error=true";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signup(@RequestParam(required = false, name = "lang") String language, Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		
		return "login/register";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String signup(@ModelAttribute @Validated User user, BindingResult result, Model model) {
		signupValidator.validate(user, result);
		if(result.hasErrors()) {
			model.addAttribute("user", user);
			model.addAttribute("lang", langService.getActualLocaleLang( null ));
			return "login/register";
		}
		userService.saveUser(user);
		
		return "redirect:/login";
	}
	
}
