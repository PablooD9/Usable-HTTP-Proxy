package com.proxy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proxy.model.UserConfiguration;

/** Clase usada para ayudar a implementar tests de integración.
 * @author Pablo
 *
 */
@Controller
public class TestController {
	
	@RequestMapping("/testUserAgent")
	public String testUserAgent(@RequestHeader(value = "User-Agent") String userAgentHeader, Model model) {
		String userAgent = UserConfiguration.getInstance().getConfiguration().getUserAgent();
		if (userAgent != null) {
			model.addAttribute("userAgent", userAgent);
		}
		else {
			model.addAttribute("userAgent", userAgentHeader);
		}

		return "test/userAgent";
	}
	
	@RequestMapping("/testCookieHeader")
	public String testCookieHeader(Model model) {
		String noCookieHeaderIsApplied = UserConfiguration.getInstance().getConfiguration().getCheckIfCookieHeader();
		if (noCookieHeaderIsApplied != null && noCookieHeaderIsApplied.equalsIgnoreCase("true")) {
			model.addAttribute("cookieHeader", "Cookies are not sent or recieved.");
		}
		else {
			model.addAttribute("cookieHeader", "Cookies are sent.");
		}

		return "test/cookieHeader";
	}
	
}
