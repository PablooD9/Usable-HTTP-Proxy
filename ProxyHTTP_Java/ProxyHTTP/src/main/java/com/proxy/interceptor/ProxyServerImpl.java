package com.proxy.interceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class ProxyServerImpl implements IProxyServer
{
	private ServerSocket serverSocket;
	private IProxyConfiguration proxyConfig;
	private final static Logger LOG = Logger.getLogger(ProxyServerImpl.class);
	
	public ProxyServerImpl(ServerSocket serverSocket, IProxyConfiguration proxyConfig) {
		this.serverSocket = serverSocket;
		this.proxyConfig = proxyConfig;
	}
	
	@Override
	public void runServer() {
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
			LOG.log(Level.ERROR, "Error de entrada/salida. " + ioe.getMessage());
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}
	}
	
	public void configureSocket(Socket socket) {
		try {
			socket.setSoTimeout( proxyConfig.getSocketTimeOut() );
			
		} catch (SocketException e) {
			LOG.log(Level.ERROR, "Error al modificar propiedades del Socket. " + e.getMessage());
		}
	}

}
