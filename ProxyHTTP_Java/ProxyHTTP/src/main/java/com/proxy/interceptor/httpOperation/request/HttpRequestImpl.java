package com.proxy.interceptor.httpOperation.request;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import com.proxy.interceptor.AbstractHttpOperation;

/**
 * @author Pablo
 *
 */
public class HttpRequestImpl extends AbstractHttpOperation implements IHttpRequest {

	private String method;
	private String requestedResource;
	private String httpVersion;
	private int port = -1;
	private boolean isSSL;
	private final static Logger LOG = Logger.getLogger(HttpRequestImpl.class);
	
	public int getPort() {
		return port;
	}
	
	public String getMethod() {
		return method;
	}

	void setMethod(String method) {
		this.method = method;
	}

	public String getRequestedResource() {
		return requestedResource;
	}

	void setRequestedResource(String requestedResource) {
		this.requestedResource = requestedResource;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}
	
	public void setSSL(boolean ssl) {
		this.isSSL = ssl;
	}
	
	public boolean isSSL() {
		return isSSL;
	}
	
	
	public HttpRequestImpl() {
		super();
	}
	
	private String getKeyOfHeader(String headerLine) {
		String separator = " *: *";
		String key = headerLine.split(separator, 2)[0];
		
		return key;
	}
	
	private String getValuesOfHeader(String headerLine) {
		String separator = " *: *";
		String values = headerLine.split(separator, 2)[1];
		
		return values;
	}
	
	@Override
	public void buildRequest(String headerLines) {
		String[] headersSplitted = headerLines.split("\r\n");
		
		try {
			loadFirstReqLine(headersSplitted[0]);
		} catch(IllegalStateException ignore) {
			return ;
		}
		
		int counter=1;
		while(headersSplitted.length > counter 
				&& headersSplitted[counter] != null 
				&& !headersSplitted[counter].equals("") )
		{
			loadHeader(headersSplitted[counter++]);
		}
		
		if (port == -1){
			if (isSSL())
				port = 443;
			else
				port = 80;
		}
			
		if (getHeaders().isEmpty())
			return;
		
		setHost( getHeaders().stream().filter(header -> header.getKey().equalsIgnoreCase("host"))
								.findFirst()
								.orElse(null)
								.getValues() );
		
		if (getHost().contains(":"))
		{
			port = Integer.parseInt(getHost().split(":")[1]);
			setHost( getHost().split(":")[0] );
		}
		
	}
	
	
	/** Método que carga en las correspondientes propiedades el contenido de la primera línea de
	 * una petición HTTP. La primera línea contiene:
	 * - Método HTTP utilizado.
	 * - Recurso solicitado al Host.
	 * - Puerto remoto.
	 * - Versión del protocolo HTTP.
	 * Ejemplos:
	 * 		CONNECT www.google.com:443 HTTP/1.1
	 *  	GET /test.html HTTP/1.1
	 * @param firstLine Primera línea de una petición HTTP.
	 */
	private void loadFirstReqLine(String firstLine) {
		String[] splittedFirstLine = firstLine.trim().split("[ ]+");
		
		if (!(splittedFirstLine.length==3)) {
			throw new IllegalStateException();
		}
		
		String method = splittedFirstLine[0];
		this.method = method;
		
		String resource = splittedFirstLine[1];
		
		if (method.equals("CONNECT")) {
			this.requestedResource = resource.split(":")[0];
			this.port = Integer.parseInt( resource.split(":")[1] );
		}
		else {
			parseResource( resource );
		}
		
		this.httpVersion = splittedFirstLine[2];
	}
	
	/** Método que comprueba que el recurso solicitado tiene un formato
	 * correcto.
	 * @param resource Recurso a parsear.
	 */
	private void parseResource(String resource) {
		if (!resource.startsWith("/")) {
			URI uri;
			try {
				uri = new URI(resource);
				String resourcePath = uri.getPath();
				String query = uri.getQuery();
				if (query != null) {
					requestedResource = resourcePath + "?" + query;
				}
				else
					requestedResource = resourcePath;
			} catch (URISyntaxException e) {
				LOG.log(Level.ERROR, "La URI no está correctamente formateada. " + e.getMessage());
			}
		}
		else {
			requestedResource = resource;
		}
	}

	/** Método que carga en la lista de cabeceras definida en la superclase la
	 * cabecera pasada por parámetro.
	 * @param headerLine Cabecera a añadir en la lista de cabeceras.
	 */
	private void loadHeader(String headerLine) {
		String key = getKeyOfHeader(headerLine);
		String values = getValuesOfHeader(headerLine);
		if (values == null) {
			getHeaders().add(new Header(key, "null"));
		}
		else
			getHeaders().add(new Header(key, values));
	}

}
