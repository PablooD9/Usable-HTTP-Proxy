package com.proxy.entity.request;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pablo
 *
 */
public class HttpRequestImpl implements HttpRequest {

	private String method;
	private String requestedResource;
	private String httpVersion;
	private String host;
	private int port = -1;
	private Header[] headers;
	private String[] body;
	private boolean isSSL;
	
	private byte[] headersByte;
	private InputStream reqContent;
	
	private final static String ASCII = "ASCII";
	
	
	@Override
	public void setContent(InputStream content) {
		this.reqContent = content;
	}

	@Override
	public InputStream getContent() {
		return reqContent;
	}
	
	public byte[] getHeadersByte() {
		return headersByte;
	}
	
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

	public String getHost() {
		return host;
	}

	public String[] getBody() {
		return body;
	}

	void setBody(String[] body) {
		this.body = body;
	}
	
	public void setSSLConnection(boolean sslConnection) {
		this.isSSL = sslConnection;
	}
	
	public boolean isSSL() {
		return isSSL;
	}
	
	public Header[] getHeaders() {
		return headers;
	}
	
	public void loadHeaders(byte[] headersByte) {
		this.headersByte = headersByte;
		String[] headerLines = getHeaderLines(headersByte);
		Header[] headers = getHeaders( headerLines );
		
		if (port == -1)
		{
			if (isSSL())
				port = 443;
			else
				port = 80;
		}
			
		if (headers == null)
			return;
		
		this.headers = headers;
		for (int i=0; i< this.headers.length; i++) {
			if (headers[i].getKey().equalsIgnoreCase("host")) 
			{
				headers[i].setValues( headers[i].getValues().split(":")[0] );
				host = headers[i].getValues();
				break;
			}
		}
		
		System.out.print("Method: " + method + ", ");
		System.out.println("Requested Resource: " + requestedResource + ", ");
		System.out.print("HttpVersion: " + httpVersion + ", ");
		System.out.print("Host: " + host + ", ");
		System.out.println("Port: " + port + ".");
	}
	
	public Header getHeader(String name) {
		if (headers != null) {
			for (int i=0; i< headers.length; i++) {
				if (headers[i].getKey().equalsIgnoreCase(name))
					return headers[i];
			}
			return null;
		}
		
		throw new IllegalStateException("Impossible to get the list of headers.");
	}
	
	private String[] getHeaderLines(byte[] headersByte) {
		List<String> headerLines = new ArrayList<>();
		byte[] separators = new byte[] {'\r', '\n'};
		
		int sep, start = 0;
		boolean isFirstLine = true;
		while ((sep = indexOf(separators, start, headersByte)) > -1) {
			String line = null;
			try {
				line = new String(headersByte, start, sep-start, ASCII);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isFirstLine) {
				loadFirstReqLine(line);
				isFirstLine=false;
			}
			else if (line != null)
				headerLines.add(line);
			start = sep + separators.length;
		}
		
		return headerLines.toArray(new String[headerLines.size()]);
	}
	
	
	private Header[] getHeaders(String[] headerLines) {
		if (headerLines.length <= 0)
			return null;
		
		List<Header> headers = new ArrayList<>();
		for(int i=0; i< headerLines.length; i++) {
			if (headerLines[i].length()>0) {
				String key = getKeyOfHeader(headerLines[i]);
				String values = getValuesOfHeader(headerLines[i]);
				
				headers.add(new Header(key, values));
			}
		}
		
		return headers.toArray(new Header[headers.size()]);
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
	
	
	/**
	 * Finds the first occurrence of separator, starting at start
	 * 
	 * @param separator
	 * @param start
	 * @return
	 */
	private int indexOf(byte[] separator, int start, byte[] headersByte) {
		if (headersByte == null)
			throw new NullPointerException("array is null");
		if (headersByte.length - start < separator.length)
			return -1;
		int sep = start;
		int i = 0;
		while (sep <= headersByte.length - separator.length
				&& i < separator.length) {
			if (headersByte[sep + i] == separator[i]) {
				i++;
			} else {
				i = 0;
				sep++;
			}
		}
		if (i == separator.length)
			return sep;
		return -1;
	}

	public void setContent() {
		
	}

	
	
	/** CONNECT www.google.com:443 HTTP/1.1
	 *  GET /test.html HTTP/1.1
	 * @param firstLine
	 */
	private void loadFirstReqLine(String firstLine) {
//		System.out.println(firstLine);
		String[] splittedFirstLine = firstLine.trim().split("[ ]+");
		
		if (!(splittedFirstLine.length==3))
			throw new IllegalStateException("Bad request format.");
		
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

	
	private void parseResource(String resource) {
		if (!resource.startsWith("/")) {
			URI uri;
			try {
				uri = new URI(resource);
				String resourcePath = uri.getPath();
				requestedResource = resourcePath;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			requestedResource = resource;
	}

	
	/* Crear métodos para, a partir de las cabeceras recopiladas:
		
		1º. ARREGLAR BUG: la cabecera de la petición (CONNECT http://.... http/1.1), ahora mismo, está incluida dentro de la lista "headers". Hay que quitarla de ahí
		y, en su lugar, rellenar las propiedades "method", "requestedResource" y "httpVersion" con lo que se vea en esa línea.
		2º. Obtener host a partir de la cabecera "Host".
		3º. Obtener el body (esto en principio será algo más complejo).
		
		
	*/
	
	
	
	
	
	
}
