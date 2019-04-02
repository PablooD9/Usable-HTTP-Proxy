package com.uniovi.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationController {
	
	@RequestMapping("/mark/list")
	public String getList() {
		return "Getting List";
	}

	@RequestMapping("/mark/add")
	public String setMark() {
		return "Adding Mark";
	}

	@RequestMapping("/mark/details ")
	public String getDetail() {
		return "Getting Details";
	}
	
}
