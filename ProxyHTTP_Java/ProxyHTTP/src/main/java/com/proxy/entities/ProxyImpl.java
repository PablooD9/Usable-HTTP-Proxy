package com.proxy.entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyImpl extends Thread implements Proxy {

	public ProxyImpl() {}
	
	@Override
	public void connection() {
		this.start();
	}
	
	@Override
    public void run() {
		ServerSocket serverSocket=null;
		try {
			serverSocket = new ServerSocket( ProxyConfig.getInstance().getPuertoLocal() );
		} catch (IOException e1) {
			// TODO: A un log.
			e1.printStackTrace();
		}
		
		Socket socket;
		Connection connectionCreator;
		try {
    		// Cuando haya una conexion abierta en el socket servidor, la abrimos/aceptamos y la manejamos.
            while ( (socket = serverSocket.accept() ) != null) {
            	connectionCreator = new ConnectionImpl( new ClientConnectionImpl(socket) );
//            	connectionCreator.connection();
            	connectionCreator.start();
            }
        } catch (IOException e) {
        	// TODO: A un log.
            e.printStackTrace(); 
        }
	}
}
