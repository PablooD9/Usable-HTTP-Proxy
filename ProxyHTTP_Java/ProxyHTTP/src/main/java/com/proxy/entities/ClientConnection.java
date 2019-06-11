package com.proxy.entities;

import java.net.Socket;

public abstract class ClientConnection extends Connection
{
	private Socket clientSocket;
	private String HTTPVersion = "1.1";
	
	
	public ClientConnection(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	public String getHTTPVersion() {
		return HTTPVersion;
	}
	void setHTTPVersion(String HTTPVersion) {
		this.HTTPVersion= HTTPVersion;
	}
	
	
	boolean connectionIsValid() {
		return (getHTTPVersion() == null || getHost() == null) ? false : true;
	}
	
	abstract void sendRequest();
}
