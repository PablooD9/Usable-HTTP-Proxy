package com.proxy.interceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.proxy.interceptor.certificate.SSLManager;

/** Clase encargada de manejar las peticiones hacia hosts que implementan HTTPS.
 * @author Pablo
 *
 */
public class SSLConnectionHandler extends AbstractSecureConnectionHandler {

	private ConnectionHandler connHandler;
	
	@Override
	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConn) {
		SSLManager.getInstance().generateEndEntityCertificate(hostTarget.getHostName());
		
		SSLSocket sslSocket = createSSLSocketConnection(socket, hostTarget.getHostName());
		
		if (sslSocket == null)
			return; // cuidao.
		
		connHandler.handleConnection(sslSocket, hostTarget, true);
	}
	
	
	private SSLSocket createSSLSocketConnection(Socket socket, String hostName) {
		
		SSLSocketFactory sslSocketFactory;
		
		SSLContext context = createSSLContext(hostName);
		sslSocketFactory = context.getSocketFactory();
	
		return startHandshake(sslSocketFactory, socket);
	}
	
	
	/** Método encargado de iniciar la comunicación segura con un Host que implementa HTTPS.
	 * @param sf Crea un SSLSocket a partir de un Socket.
	 * @param socket Socket que será transformado en un SSLSocket.
	 * @return SSLSocket.
	 */
	private SSLSocket startHandshake(SSLSocketFactory sf, Socket socket) {
		
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) sf.createSocket(socket, socket
					.getInetAddress().getHostName(), socket.getPort(), true); // true -> autoclose SSLSocket
			sslSocket.setUseClientMode(false);
			sslSocket.setEnabledProtocols(getEnabledProtocols());
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
		return SSLManager.getInstance().generateContext(host);
	}

}
