package com.proxy.entity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.proxy.entity.certificate.SSLManager;
import com.proxy.entity.request.HttpRequest;

public class SSLConnectionHandler implements ConnectionHandler {

	private SSLManager sslManager;
	private ConnectionHandler connHandler;
	
	public SSLConnectionHandler(SSLManager sslManager) {
		this.sslManager = sslManager;
	}
	
	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConn) {
		sslManager.generateEndEntityCert(hostTarget.getHostName());
		
		SSLSocket sslSocket = createSSLSocketConnection(socket, hostTarget.getHostName());
		
		connHandler.handleConnection(sslSocket, hostTarget, true);
	}
	
	
	private SSLSocket createSSLSocketConnection(Socket socket, String hostName) {
		
		SSLContext sslContext;
		SSLSocketFactory sslSocketFactory = null;
		try {
			sslContext = sslManager.generateContext(hostName);
			sslSocketFactory = sslContext.getSocketFactory();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return startHandshake(sslSocketFactory, socket);
	}
	
	
	private SSLSocket startHandshake(SSLSocketFactory sf, Socket socket) {
		
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) sf.createSocket(socket, socket
					.getInetAddress().getHostName(), socket.getPort(), true); // true -> autoclose SSLSocket
			sslSocket.setUseClientMode(false);
			sslSocket.startHandshake();
			
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
		}
		
		return sslSocket;
	}
	
}
