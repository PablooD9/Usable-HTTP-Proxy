package com.proxy.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/** Clase que representa una Configuración de un usuario en la aplicación.
 * Esta clase contiene las opciones que pueden ser aplicadas por un usuario
 * desde la página de configuración de la aplicación.
 * @author Pablo
 *
 */
@Document(collection = "Configuration")
public class Configuration {
	@Id
	private String email;
	
	private String OS;
	private String browser;
	private String userAgent;
	private String checkIfSpanishMaliciousHosts;
	private String checkIfMaliciousHosts;
	private String checkIfTrackersHosts;
	private String checkIfPornographicHosts;
	private String checkIfCookieHeader;
	private String securityHeaders;
	@Transient private List<String> hostExceptions = new ArrayList<String>();
	
	public Configuration() {}
	
	public Configuration(String userEmail, String op1_os, String op1_browser, String op2, String op3, String op4, String op5, String op6, String op7) {
		setEmail(userEmail);
		setOS(op1_os);
		setBrowser(op1_browser);
		setCheckIfSpanishMaliciousHosts(op2);
		setCheckIfMaliciousHosts(op3);
		setCheckIfTrackersHosts(op4);
		setCheckIfPornographicHosts(op5);
		setSecurityHeaders(op6);
		setCheckIfCookieHeader(op7);
//		hostExceptions = new ArrayList<>();
	}
	

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getOS() {
		return OS;
	}

	public void setOS(String op1_os) {
		this.OS = op1_os;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String op1_browser) {
		this.browser = op1_browser;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String op1) {
		this.userAgent = op1;
	}

	public String getCheckIfSpanishMaliciousHosts() {
		return checkIfSpanishMaliciousHosts;
	}

	public void setCheckIfSpanishMaliciousHosts(String op2) {
		this.checkIfSpanishMaliciousHosts = op2;
	}

	public String getCheckIfMaliciousHosts() {
		return checkIfMaliciousHosts;
	}

	public void setCheckIfMaliciousHosts(String op3) {
		this.checkIfMaliciousHosts = op3;
	}

	public String getCheckIfTrackersHosts() {
		return checkIfTrackersHosts;
	}

	public void setCheckIfTrackersHosts(String op4) {
		this.checkIfTrackersHosts = op4;
	}

	public String getCheckIfPornographicHosts() {
		return checkIfPornographicHosts;
	}

	public void setCheckIfPornographicHosts(String op5) {
		this.checkIfPornographicHosts = op5;
	}

	public String getSecurityHeaders() {
		return securityHeaders;
	}

	public void setSecurityHeaders(String op6) {
		this.securityHeaders = op6;
	}

	public List<String> getHostExceptions() {
		return hostExceptions;
	}

	public void setHostExceptions(List<String> hostExceptions) {
		this.hostExceptions = hostExceptions;
	}

	public String getCheckIfCookieHeader() {
		return checkIfCookieHeader;
	}

	public void setCheckIfCookieHeader(String checkIfCookieHeader) {
		this.checkIfCookieHeader = checkIfCookieHeader;
	}
	
}
