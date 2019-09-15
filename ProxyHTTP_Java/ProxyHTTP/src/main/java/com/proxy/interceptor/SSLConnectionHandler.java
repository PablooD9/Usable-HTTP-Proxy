package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.proxy.interceptor.certificate.SSLManager;

public class SSLConnectionHandler extends SecureConnectionHandler {

	private SSLManager sslManager;
	private ConnectionHandler connHandler;
	private String[] enabledProtocols = { "TLSv1", "TLSv1.1", "TLSv1.2" };
	
	public SSLConnectionHandler() {
		this.sslManager = new SSLManager();
	}
	
	@Override
	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConn) {
		sslManager.generateEndEntityCert(hostTarget.getHostName());
		
		SSLSocket sslSocket = createSSLSocketConnection(socket, hostTarget.getHostName());
		
		if (sslSocket == null)
			return;
		
		connHandler.handleConnection(sslSocket, hostTarget, true);
	}
	
	
	private SSLSocket createSSLSocketConnection(Socket socket, String hostName) {
		
		SSLSocketFactory sslSocketFactory;
		
		SSLContext context = createSSLContext(hostName);
		sslSocketFactory = context.getSocketFactory();
	
		return startHandshake(sslSocketFactory, socket);
	}
	
	
	private SSLSocket startHandshake(SSLSocketFactory sf, Socket socket) {
		
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) sf.createSocket(socket, socket
					.getInetAddress().getHostName(), socket.getPort(), true); // true -> autoclose SSLSocket
			sslSocket.setUseClientMode(false);
			sslSocket.setEnabledProtocols( enabledProtocols );
			sslSocket.startHandshake();
			return sslSocket;
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
		}
		return null;
	}

	@Override
	public SSLContext createSSLContext(String host) {
		return sslManager.generateContext(host);
	}

}
