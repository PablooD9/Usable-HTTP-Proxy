package com.proxy.entity;


import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;

public abstract class SecureConnectionHandler extends Thread implements ConnectionHandler {
	
	public abstract SSLContext createSSLContext(String host);
	
	public void setSSLConnection(boolean ssl)
	{
		// TODO
		throw new IllegalStateException("What happened here? SSL CONNECTION (bool)");
	}
	
	public void setHostTarget(InetSocketAddress hostTarget)
	{
		// TODO
		throw new IllegalStateException("What happened here? HOSTTARGET");
	}
	
	public void setSocket(Socket socket)
	{
		// TODO
		throw new IllegalStateException("What happened here? SOCKET");
	}
}
