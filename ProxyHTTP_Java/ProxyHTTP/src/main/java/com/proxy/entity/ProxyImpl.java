package com.proxy.entity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.proxy.entity.certificate.SSLManager;
import com.proxy.entity.request.HttpRequest;

public class ProxyImpl implements Proxy {

	public ProxyImpl() {}
	
	@Override
	public void connection() {
		ServerSocket serverSocket=null;
		InetSocketAddress localConn = new InetSocketAddress(ProxyConfig.getInstance().getHost(), ProxyConfig.getInstance().getLocalPort());
		
		try {
			serverSocket = new ServerSocket( localConn.getPort(), ProxyConfig.getInstance().getMaxNumOfClientsReqWaiting(), localConn.getAddress() );
			serverSocket.setReuseAddress( true );
			System.out.println( "=========================== Listening on port " + serverSocket.getLocalPort() + " =============================");
		} catch (IOException e1) {
			// TODO: A un log.
			e1.printStackTrace();
		}
		
		SSLManager manCerts = new SSLManager();
		
		ConnectionHandler sslConnectionHandler = new SSLConnectionHandler( manCerts );
		ConnectionHandler proxyConnHandler = new ConnectionHandlerImpl( sslConnectionHandler );
		sslConnectionHandler.setConnectionHandler( proxyConnHandler );
		
		Connection connection = new Connection(proxyConnHandler);
		connection.createConnection( serverSocket );
		connection.startProxy();
	}

}
