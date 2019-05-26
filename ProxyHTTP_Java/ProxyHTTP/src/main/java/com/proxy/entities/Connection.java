package com.proxy.entities;

public abstract class Connection extends Thread implements Proxy
{
	private Connection clientConn;
	private Connection serverConn;
	private final String ISO = "ISO-8859-1"; // Norma ISO correspondiente al alfabeto latino (�, �, �, etc.)
	
	public Connection() {}
	
	public Connection(Connection client) {
		this.clientConn = client;
		this.serverConn = new ServerConnectionImpl();
	}
	
	public String getISO() {
		return ISO;
	}
	
	@Override
	public void run() {
		connection();
	}
	
	@Override
	public void connection() {
		clientConn.manageConnection( null );
		serverConn.manageConnection( clientConn );
	}
	
	abstract void manageConnection(Connection conn);
	
}
