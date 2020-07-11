package com.proxy.interceptor.error;

import java.io.UnsupportedEncodingException;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class HttpErrorPageImpl implements IHttpErrorPage {

	private String host, message;
	
	private final byte[] ERROR_STATUS_LINE = "HTTP/1.1 500 Proxy Error\r\n".getBytes();
	
	private final byte[] ERROR_HEADERS = "Content-Type: text/html\r\nConnection: close\r\n\r\n".getBytes();

	private final String ERROR_BODY1 = 
			"<html>"
				+ "<head>"
					+ "<title>Proxy Error</title>"
				+ "</head>"
				+ "<body>"
					+ "<h1>Proxy Error</h1>";

	private final String ERROR_BODY2 = "</body></html>";
	private final static Logger LOG = Logger.getLogger(HttpErrorPageImpl.class);
	
	public HttpErrorPageImpl(String message, String host) {
		this.message = message;
		this.host = host;
	}
	
	@Override
	public byte[] getStatusLine() {
		return ERROR_STATUS_LINE;
	}

	@Override
	public byte[] getHeaders() {
		return ERROR_HEADERS;
	}

	@Override
	public byte[] getBody() {
		String body = ERROR_BODY1;
		body += getBodyMessage();
		body += ERROR_BODY2;
		return getBytes( body );
	}
	
	private byte[] getBytes(String string) {
		try {
			return string.getBytes("ASCII");
		} catch (UnsupportedEncodingException uee) {
			LOG.log(Level.INFO, "No se ha podido codificar correctamente la cadena pasada por parámetro.");
			return null;
		}
	}
	
	private String getBodyMessage() {
		String msg = "<p>Problem description with the host " + host + ":</p>"
						+"<p>" + message + "</p>";
		String msg2 = "<a href=http://localhost:8090/addSecurityException?host=" + host + ">" + 
					"    <span>" + 
					"        Click here to skip this warning and add an exception to this website." + 
					"    </span>" + 
					  "</a>";
		
		return msg + msg2;
	}

}
