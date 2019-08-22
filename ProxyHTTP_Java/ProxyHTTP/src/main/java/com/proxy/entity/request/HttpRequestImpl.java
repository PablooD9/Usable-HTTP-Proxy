package com.proxy.entity.request;

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
//	private Header[] headers;
	private List<Header> headers;
	private String body;
	private boolean isSSL;
	
	private byte[] headersByte;
	
//	private final static String ASCII = "ASCII";
	
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
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
	
//	public Header[] getHeaders() {
//		return headers;
//	}
	
	/*
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
		System.out.print("Requested Resource: " + requestedResource + ", ");
		System.out.print("HttpVersion: " + httpVersion + ", ");
		System.out.print("Host: " + host + ", ");
		System.out.println("Port: " + port + ".");
		
	}
	*/
	
	/*
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
	*/
	
	/*
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
	*/
	
	/*
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
	*/
	
	
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
	/*private int indexOf(byte[] separator, int start, byte[] headersByte) {
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
	*/
	
	@Override
	public void parse(String headerLines) {
		String[] headersSplitted = headerLines.split("\r\n");
		
		try {
			loadFirstReqLine(headersSplitted[0]);
		} catch(IllegalStateException ise) {
			// TODO
			System.err.println("EMPTY REQUEST");
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
		
		System.out.print("Method: " + method + ", \t");
		System.out.print("Requested Resource: " + requestedResource + ", \t");
		System.out.print("HttpVersion: " + httpVersion + ", \t");
		System.out.print("Host: " + host + ", \t");
		System.out.println("Port: " + port + ".");
		
	}
	
	
	/** CONNECT www.google.com:443 HTTP/1.1
	 *  GET /test.html HTTP/1.1
	 * @param firstLine
	 */
	private void loadFirstReqLine(String firstLine) {
		System.err.println("esto: " + firstLine + "en Thread::>>>> " + Thread.currentThread().getName());
		String[] splittedFirstLine = firstLine.trim().split("[ ]+");
		
		if (!(splittedFirstLine.length==3)) {
			if (splittedFirstLine.length==2)
				System.err.println("LONGITUD 2: " + splittedFirstLine[0] + "" + splittedFirstLine[1]);
			else if (splittedFirstLine.length==1)
				System.err.println("LONGITUD 1: " + splittedFirstLine[0]);

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
