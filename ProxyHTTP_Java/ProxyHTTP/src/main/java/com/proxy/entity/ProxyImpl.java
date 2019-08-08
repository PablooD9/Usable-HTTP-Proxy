package com.proxy.entity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class ProxyImpl implements Proxy {

	@Override
	public void connection() {
		setSystemProperties();
		
		ServerSocket serverSocket = null;
		InetSocketAddress localConn = new InetSocketAddress(ProxyConfig.getInstance().getHost(), ProxyConfig.getInstance().getLocalPort());
		
		try {
			serverSocket = new ServerSocket( localConn.getPort(), ProxyConfig.getInstance().getMaxNumOfClientsReqWaiting(), localConn.getAddress() );
			serverSocket.setReuseAddress( true );
			System.out.println( "=========================== Listening on port " + serverSocket.getLocalPort() + " =============================");
		
			
			SecureConnectionHandler sslConnectionHandler = new SSLConnectionHandler();
			ConnectionHandler proxyConnHandler = new ConnectionHandlerImpl( sslConnectionHandler );
			sslConnectionHandler.setConnectionHandler(proxyConnHandler);
			Connection connection;
//          connection.startProxy();
		
			
			
			Socket socket;
			try {
				while ((socket = serverSocket.accept()) != null) {
					configureSocket( socket );
					
					connection = new ConnectionImpl(proxyConnHandler);
					connection.setSocket(socket);
					connection.run();
					
//					handler.handleConnection( socket, null, false );
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (IOException e1) {
			// TODO: A un log.
			e1.printStackTrace();
		}
		
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException ioe) {
			// TODO
			ioe.printStackTrace();
		}
	}
		
	private void setSystemProperties() {
		System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,connection,content-length,expect");
		
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
