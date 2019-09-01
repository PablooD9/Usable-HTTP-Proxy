package com.proxy.entity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.proxy.entity.certificate.SSLManager;

public class ConnectionImpl implements Connection
{
	private ServerSocket serverSocket;
	
	public ConnectionImpl(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	@Override
	public void run() {
		Socket socket;
		try {
			while ( true ) {
				socket = serverSocket.accept();
				configureSocket(socket);
				ConnectionHandler handler = new ConnectionHandlerImpl( socket, new SSLConnectionHandler() );
				
				Thread thread = new Thread(handler);
				thread.start();
			}
		} catch (IOException ioe) {
			// TODO 
			ioe.printStackTrace();
		}
	}
	
	private void configureSocket(Socket socket) {
		try {
			socket.setSoTimeout( ProxyConfig.getInstance().getSocketTimeOut() );
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
