package com.proxy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proxy.services.HostService;

@Controller
public class ConfigurationController {
	
	@Autowired
	private HostService hostService;
	
	@RequestMapping("/configuration")
	public String getConfiguration(Model model) {
		// model.addAttribute("hostsList", hostService.getHostsList());
		return "configuration/config";
	}
	
	@RequestMapping("/configuration/updateMaliciousHosts")
	public String updateMaliciousHosts() {
		hostService.updateHostsList();
		return "redirect:/configuration";
	}
	
}
