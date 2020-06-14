package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ProxyConnectionImpl implements IProxyConnection {
	
	@Override
	public void establishConnection() {
		int port = 8080;
		int socketTimeOut = 18000;
		IProxyConfiguration proxyConfig = new ProxyConfigurationImpl( port, socketTimeOut );
		
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
		
		IProxyServer connection = new ProxyServerImpl(serverSocket, proxyConfig);
		connection.runServer();

	}
		
}
