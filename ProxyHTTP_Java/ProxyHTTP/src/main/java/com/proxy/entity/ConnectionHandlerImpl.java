package com.proxy.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

import com.proxy.entity.io.ChunkedInputStream;
import com.proxy.entity.io.CopyInputStream;
import com.proxy.entity.io.FixedLengthInputStream;
import com.proxy.entity.request.Header;
import com.proxy.entity.request.HttpRequest;
import com.proxy.entity.request.HttpRequestImpl;

public class ConnectionHandlerImpl implements ConnectionHandler {

	private final byte[] HTTP_OK = "HTTP/1.0 200 OK\r\n\r\n".getBytes();
	
	private ConnectionHandler connHandler; // useful for SSL communications
	
	public ConnectionHandlerImpl(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}


	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConnection) {
	
		InetAddress source = socket.getInetAddress();
		
		InputStream inStream;
		OutputStream outStream; 
		try {
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
			
			boolean close = false;
			String version = null, connection = null;
//			while (!close) {
				HttpRequest request = null;
				request = readRequest(inStream, sslConnection);
				if (request == null)
					return ;
				
				if (request.getMethod().equalsIgnoreCase("CONNECT")) { // SSL Connection? Probably.
					connectMethod( socket, request );
					return ;
				}
//			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
	public HttpRequest readRequest(InputStream in, boolean sslConnection) throws IOException {

		HttpRequest request = new HttpRequestImpl();
		request.setSSLConnection( sslConnection );
		
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
		CopyInputStream cis = new CopyInputStream(in, copy);
		try {
			String line;
			do {
				line = cis.readLine();
			} while (line != null && !"".equals(line));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			byte[] headerBytes = copy.toByteArray();
			if (headerBytes == null || headerBytes.length == 0)
				return null;
		}
		
		byte[] headerBytes = copy.toByteArray();
		
		// empty request line, connection closed?
		if (headerBytes == null || headerBytes.length == 0)
			return null;
		
		request.loadHeaders( headerBytes );
		if (request.getHeaders() == null)
			return null;
		
		Header transferCodingHeader = request.getHeader("Transfer-Encoding");
		Header contentLengthHeader = request.getHeader("Content-Length");
		
		if (transferCodingHeader != null
				&& transferCodingHeader.getValues().trim().equalsIgnoreCase("chunked")) 
		{
			in = new ChunkedInputStream(in, true); // don't unchunk
		} 
		else if (contentLengthHeader != null) {
			try {
				in = new FixedLengthInputStream(in, Integer
						.parseInt(contentLengthHeader.getValues()));
			} catch (NumberFormatException nfe) {
				IOException ioe = new IOException(
						"Invalid content-length header: " + contentLengthHeader.getValues());
				ioe.initCause(nfe);
				throw ioe;
			}
		}
		else {
			in = null;
		}
		
		request.setContent( in );
		
		return request;
	}
	
	private void connectMethod( Socket socket, HttpRequest request ) {
		try {
			OutputStream outStream = socket.getOutputStream();
			outStream.write(HTTP_OK);
			outStream.flush();
			
			InetSocketAddress hostTarget = new InetSocketAddress(request.getHost(), request.getPort());
			connHandler.handleConnection(socket, hostTarget, request.isSSL());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
