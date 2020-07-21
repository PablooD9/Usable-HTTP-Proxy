package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class ProxyConnectionImpl implements IProxyConnection {
	
	private final static Logger LOG = Logger.getLogger(ProxyConnectionImpl.class);
	
	@Override
	public void establishConnection() {
		int port = 8080;
		int socketTimeOut = 60000;
		IProxyConfiguration proxyConfig = new ProxyConfigurationImpl( port, socketTimeOut );
		
		InetSocketAddress localConn = new InetSocketAddress(proxyConfig.getLocalhost(), proxyConfig.getLocalPort());
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket( localConn.getPort(), proxyConfig.getMaxNumOfClientsReqWaiting(), localConn.getAddress() );
			serverSocket.setReuseAddress( true );
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de Entrada/Salida al crear el ServerSocket. No se puede iniciar la aplicación." + " " + e.getMessage());
		}
		
		LOG.log(Level.INFO, "Proxy escuchando en el puerto " + serverSocket.getLocalPort());
		
		IProxyServer connection = new ProxyServerImpl(serverSocket, proxyConfig);
		connection.runServer();

	}
		
}
