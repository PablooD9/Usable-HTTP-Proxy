package com.proxy.entities;

import java.net.Socket;

public abstract class ClientConnection extends Connection
{
	private Socket clientSocket;
	private String URL;
	private String HTTPVersion = "1.1";
	private int serverPort;
	
	public ClientConnection(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	public String getHostURL() {
		return URL;
	}
	void setHostURL(String uRL) {
		URL = uRL;
	}
	public String getHTTPVersion() {
		return HTTPVersion;
	}
	void setHTTPVersion(String HTTPVersion) {
		this.HTTPVersion= HTTPVersion;
	}
	public int getServerPort() {
		return serverPort;
	}
	void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	
	boolean connectionIsValid() {
		return (getHTTPVersion() == null || getHostURL() == null) ? false : true;
	}
	
	abstract void sendRequest();
}
