package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class ProxyImpl implements Proxy {
	
	@Override
	public void establishConnection() {
		InetSocketAddress localConn = new InetSocketAddress(ProxyConfig.getInstance().getHost(), ProxyConfig.getInstance().getLocalPort());
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket( localConn.getPort(), ProxyConfig.getInstance().getMaxNumOfClientsReqWaiting(), localConn.getAddress() );
			serverSocket.setReuseAddress( true );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println( "=========================== Listening on port " + serverSocket.getLocalPort() + " =============================");
		
		Connection connection = new ConnectionImpl(serverSocket);
		connection.run();

	}
		
}
