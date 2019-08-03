package com.proxy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ConfigurationController {
	
//	@Autowired
//	private HostService hostService;
	
	@RequestMapping(value={"", "/", "/configuration"})
	public String getConfiguration(Model model) {
		// model.addAttribute("hostsList", hostService.getHostsList());
		return "configuration/configuration";
	}
	
	@RequestMapping("/configuration/updateMaliciousHosts")
	public String updateMaliciousHosts() {
//		hostService.updateHostsList();
		return "redirect:/";
	}
	
}
