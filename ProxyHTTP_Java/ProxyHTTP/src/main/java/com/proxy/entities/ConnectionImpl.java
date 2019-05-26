package com.proxy.entities;

public class ConnectionImpl extends Connection {

	public ConnectionImpl(Connection clientConn) {
		super( clientConn );
	}
	
	@Override
	void manageConnection(Connection conn) {
		Thread thread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	connection();
		    }
		});  
		thread.start();
		
	}

}
