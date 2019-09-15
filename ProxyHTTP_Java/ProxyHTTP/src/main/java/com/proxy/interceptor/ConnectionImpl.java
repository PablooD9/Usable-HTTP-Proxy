package com.proxy.interceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;

import com.proxy.interceptor.certificate.SSLManager;

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
			var executorsPool = Executors.newFixedThreadPool(40);
			while ( true ) {
				socket = serverSocket.accept();
				configureSocket(socket);
				ConnectionHandler handler = new ConnectionHandlerImpl( socket, new SSLConnectionHandler() );
				
				executorsPool.execute( handler );
				
//				Thread thread = new Thread(handler);
//				thread.start();
			}
		} catch (IOException ioe) {
			// TODO 
			ioe.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
