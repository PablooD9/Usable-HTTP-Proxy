package com.proxy.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SecurityException")
public class SecurityException {

	@Id
	private String email;
	private String hostsException;
	
	public SecurityException() {}
	
	public SecurityException(String email, String hostsException) {
		setEmail(email);
		setHostsException( hostsException );
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getHostsException() {
		return hostsException;
	}
	public void setHostsException(String hostsException) {
		this.hostsException = hostsException;
	}
	
}
