package com.proxy.interceptor.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pablo
 *
 */
public class HttpRequestImpl implements IHttpRequest {

	private String method;
	private String requestedResource;
	private String httpVersion;
	private String host;
	private int port = -1;
	private List<Header> headers;
	private byte[] body;
	private boolean isSSL;
	
	private byte[] headersByte;
	
	public List<Header> getHeaders() {
		// TODO Auto-generated method stub
		return headers;
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
	
	public void setHost(String host) {
		this.host = host;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public void setSSL(boolean ssl) {
		this.isSSL = ssl;
	}
	
	public boolean isSSL() {
		return isSSL;
	}
	
	
	public HttpRequestImpl() {
		headers = new ArrayList<>();
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
	public void parse(String headerLines) {
		String[] headersSplitted = headerLines.split("\r\n");
		
		try {
			loadFirstReqLine(headersSplitted[0]);
		} catch(IllegalStateException ise) {
			// TODO
//			System.err.println("EMPTY REQUEST");
			return ;
//			ise.printStackTrace();
		}
		
		int counter=1;
		while(headersSplitted.length > counter 
				&& headersSplitted[counter] != null 
				&& !headersSplitted[counter].equals("") )
		{
			loadHeader( headersSplitted[counter++] );
		}
		
		if (port == -1){
			if (isSSL())
				port = 443;
			else
				port = 80;
		}
			
		if (headers.isEmpty())
			return;
		
		host = headers.stream().filter(header -> header.getKey().equalsIgnoreCase("host"))
								.findFirst()
								.orElse(null)
								.getValues();
		
		if (host.contains(":"))
		{
			port = Integer.parseInt(host.split(":")[1]);
			host = host.split(":")[0];
		}
		
	}
	
	
	/** CONNECT www.google.com:443 HTTP/1.1
	 *  GET /test.html HTTP/1.1
	 * @param firstLine
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			requestedResource = resource;
	}

	private void loadHeader(String headerLine) {
		String key = getKeyOfHeader(headerLine);
		String values = getValuesOfHeader(headerLine);
		if (values == null) {
			System.err.println("NULL FOUND FOR HEADER " + key);
			headers.add(new Header(key, "null"));
		}
		else
			headers.add(new Header(key, values));
	}

	@Override
	public Header getHeader(String name) {
		Header headerFound = headers.parallelStream().filter(header -> header.getKey().equalsIgnoreCase(name))
													 .findFirst()
													 .orElse(null);
		
		return headerFound;
	}

	@Override
	public void setHeader(String headerName, String newValue) {
		Header header;
		if (( header=getHeader(headerName)) != null) {
			header.setValues(newValue);
		}
	}

}
