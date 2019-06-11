package com.proxy.entities;

public class DesignTest {

	public static void main(String[] args) {
		
	}
	
	public void start() {
		Proxy connHandler = new ProxyImpl();
		connHandler.connection();
	}

}
