package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ProxyImpl implements Proxy {
	
	@Override
	public void establishConnection() {
		int port = 8080;
		int socketTimeOut = 60000;
		ProxyConfig proxyConfig = new ProxyConfigImpl( port, socketTimeOut );
		
		InetSocketAddress localConn = new InetSocketAddress(proxyConfig.getLocalhost(), proxyConfig.getLocalPort());
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket( localConn.getPort(), proxyConfig.getMaxNumOfClientsReqWaiting(), localConn.getAddress() );
			serverSocket.setReuseAddress( true );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println( "=========================== Listening on port " + serverSocket.getLocalPort() + " =============================");
		
		Connection connection = new ConnectionImpl(serverSocket, proxyConfig);
		connection.runServer();

	}
		
}
