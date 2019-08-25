package com.proxy.services;

import org.springframework.stereotype.Service;

@Service
public class LanguageService {
	
	private String actualLanguage;
	
	public LanguageService() {
		actualLanguage = "es";
	}

	public String getActualLocaleLang(String language) {
		if (language != null) {
			actualLanguage = language;
			return language;
		}
		else {
			return actualLanguage;
		}

	}
	
}
