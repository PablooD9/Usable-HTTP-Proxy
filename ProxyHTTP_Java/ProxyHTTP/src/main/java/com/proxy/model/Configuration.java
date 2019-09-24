package com.proxy.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Configuration")
public class Configuration {
	@Id
	private String email;
	
	private String op1_os;
	private String op1_browser;
	private String op1;
	private String op2;
	private String op3;
	private String op4;
	private String op5;
	private String op6;
	@Transient private List<String> hostExceptions;
	
	public Configuration() {}
	
	public Configuration(String userEmail, String op1_os, String op1_browser, String op2, String op3, String op4, String op5, String op6) {
		setEmail(userEmail);
		setOp1_os(op1_os);
		setOp1_browser(op1_browser);
		setOp2(op2);
		setOp3(op3);
		setOp4(op4);
		setOp5(op5);
		setOp6(op6);
		hostExceptions = new ArrayList<>();
	}
	

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getOp1_os() {
		return op1_os;
	}

	public void setOp1_os(String op1_os) {
		this.op1_os = op1_os;
	}

	public String getOp1_browser() {
		return op1_browser;
	}

	public void setOp1_browser(String op1_browser) {
		this.op1_browser = op1_browser;
	}
	
	public String getOp1() {
		return op1;
	}

	public void setOp1(String op1) {
		this.op1 = op1;
	}

	public String getOp2() {
		return op2;
	}

	public void setOp2(String op2) {
		this.op2 = op2;
	}

	public String getOp3() {
		return op3;
	}

	public void setOp3(String op3) {
		this.op3 = op3;
	}

	public String getOp4() {
		return op4;
	}

	public void setOp4(String op4) {
		this.op4 = op4;
	}

	public String getOp5() {
		return op5;
	}

	public void setOp5(String op5) {
		this.op5 = op5;
	}

	public String getOp6() {
		return op6;
	}

	public void setOp6(String op6) {
		this.op6 = op6;
	}

	public List<String> getHostExceptions() {
		return hostExceptions;
	}

	public void setHostExceptions(List<String> hostExceptions) {
		this.hostExceptions = hostExceptions;
	}
	
	
}
