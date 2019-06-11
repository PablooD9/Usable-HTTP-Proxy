package com.proxy.entities;

public class ConnectionImpl extends Connection {
	
	public ConnectionImpl() {}
	
	public ConnectionImpl(Connection clientConnectionImpl) {
		super(clientConnectionImpl);
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
	
	@Override
	public void run() {
		connection();
	}

}
