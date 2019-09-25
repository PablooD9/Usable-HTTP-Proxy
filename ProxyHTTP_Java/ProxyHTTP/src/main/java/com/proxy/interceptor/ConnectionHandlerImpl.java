package com.proxy.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.HttpStatus;

import com.proxy.interceptor.error.HttpErrorPageImpl;
import com.proxy.interceptor.error.IHttpErrorPage;
import com.proxy.interceptor.httpOperation.request.Header;
import com.proxy.interceptor.httpOperation.request.HttpRequestImpl;
import com.proxy.interceptor.httpOperation.request.IHttpRequest;
import com.proxy.interceptor.httpOperation.response.HttpResponseImpl;
import com.proxy.interceptor.httpOperation.response.IHttpResponse;
import com.proxy.model.functionality.CheckMaliciousHost;
import com.proxy.model.functionality.CheckPornographyHost;
import com.proxy.model.functionality.CheckSecurityHeaders;
import com.proxy.model.functionality.CheckTrackerHost;
import com.proxy.model.functionality.IProxyFunctionality;
import com.proxy.model.functionality.ModifyUserAgent;
import com.proxy.model.functionality.ProxyFunctionalityImpl;

public class ConnectionHandlerImpl implements ConnectionHandler {

	private final static byte[] HTTP_OK = "HTTP/1.1 200 OK\r\n\r\n".getBytes();
	
	private ConnectionHandler connHandler; // useful for SSL communications
	
	private Socket socket;
	
	public ConnectionHandlerImpl(Socket socket, ConnectionHandler connHandler) {
		setConnectionHandler(connHandler);
		connHandler.setConnectionHandler( this );
		
		this.socket = socket;
	}

	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	@Override
	public void run() {
		handleConnection(socket, null, false);
	}
	
	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConnection) {
		
    	System.out.println( "=========================== Starting new request. THREAD: " + Thread.currentThread().getName() + " =============================");
    	
    	InputStream inStream;
    	boolean error = false;
		try {
			inStream = socket.getInputStream();
			
			IHttpRequest request = null;
			request = readRequest(inStream, sslConnection);
			
			if (request.getHeaders().isEmpty()){
				error = true;
			}
			
			if (!error) {
				if (request.getMethod().equalsIgnoreCase("CONNECT")) { // SSL Connection? Probably.
					connectMethod( socket, request );
				}
				else {
					if (hostTarget == null)
						hostTarget = new InetSocketAddress(request.getHost(), request.getPort());

					boolean maliciousRequest = configureRequest(request);
					
					sendResponse(socket, request, null, maliciousRequest);
				}
				
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			closeSocket(socket);
		}
		
	}
	
	private void closeSocket(Socket socket) {
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException ignore) {}
	}
	
	private void sendResponse(Socket socket, IHttpRequest req, String uri, boolean maliciousRequest) {

		HttpClient client = null;
		if (req.isSSL()) {
			SSLContext sslContext = ((AbstractSecureConnectionHandler)connHandler).createSSLContext( req.getHost() );
			client = HttpClient.newBuilder()
	                .connectTimeout(Duration.ofSeconds(30))
	                .priority(1)
	                .version(HttpClient.Version.HTTP_2)
	                .followRedirects(Redirect.ALWAYS)
	                .sslContext( sslContext )
					.build();
		}
		else
			client = HttpClient.newBuilder()
		            .connectTimeout(Duration.ofSeconds(30))
		            .priority(1)
		            .version(HttpClient.Version.HTTP_2)
		            .followRedirects(Redirect.ALWAYS)
		            .build();
		
		String protocolAndHost = ((req.isSSL()) ? "https://" : "http://") + req.getHost();
		
		if (uri == null)
			uri = protocolAndHost + req.getRequestedResource();
		else {
			if (uri.startsWith("/"))
				uri = protocolAndHost + uri;
			System.out.println("Aqui:" + uri);
		}
		
		if (maliciousRequest)
			uri = "https://localhost:8091";
		
		HttpRequest.Builder preRequest=null;
		if (req.getMethod().equalsIgnoreCase("GET")) {
			preRequest = HttpRequest.newBuilder()  // GET request!
		        .uri(URI.create( uri ))
		        .GET();
		}
		else if (req.getMethod().equalsIgnoreCase("POST")) {
			preRequest = HttpRequest.newBuilder()  // POST request!
	        .uri(URI.create( uri ))
	        .POST(BodyPublishers.ofByteArray(req.getBody()));
		}
		else {
			if (req.getBody() != null)
				preRequest = HttpRequest.newBuilder()
				.uri(URI.create( uri ))
				.method(req.getMethod(), BodyPublishers.ofByteArray( req.getBody() ));
			else
				preRequest = HttpRequest.newBuilder()
				.uri(URI.create( uri ))
				.method(req.getMethod(), BodyPublishers.noBody());
		}
		
		for (Header header : req.getHeaders()) {
			if (!System.getProperty("restrictedHeaders").trim().toLowerCase().contains( header.getKey().trim().toLowerCase() )) 
			{
				preRequest.setHeader(header.getKey(), header.getValues());
			}
		}
//		preRequest.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36");
		HttpRequest request = preRequest.build();
		
		System.err.println("Request to: " + uri);
		
		HttpResponse<byte[]> response;
		try {
			response = client.sendAsync(request, BodyHandlers.ofByteArray())
							 .join();	
		} catch (CompletionException ce) {
			System.err.println("Address " + uri + " is unreachable!");
//			ce.printStackTrace();
			return ;
		}
		
		HttpHeaders httpHeaders = response.headers();
		
		Optional<String> locationHeader = httpHeaders.firstValue("Location"); // When resource has been permanently moved
		
		if ( !locationHeader.isEmpty() ) {
			System.out.println("Moved permanently to " + locationHeader.get());
			sendResponse( socket, req, locationHeader.get(), maliciousRequest );
		}
		else {
			IHttpResponse httpResponse = buildResponse( req, response );
			httpResponse.setHost( req.getHost() );
			
			boolean maliciousResponse = configureResponse( httpResponse );
			
			writeResponse(socket, response.body(), httpResponse, maliciousRequest, maliciousResponse);
		}
	}
	
	private IHttpResponse buildResponse(IHttpRequest request, HttpResponse<byte[]> response) {
		IHttpResponse httpResponse = new HttpResponseImpl();
		var crlf = "\r\n";
		
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
		
		protocol = protocol.replace("HTTP/2", "HTTP/1.1");
		int code = response.statusCode();
		String reasonPhrase = HttpStatus.getStatusText( code );
		var responseString = protocol + " " + code + " " + reasonPhrase;
		httpResponse.setStatusLine( responseString );
		
		responseString += crlf;
		
		HttpHeaders httpHeaders = response.headers();
		Map<String, List<String>> headers = httpHeaders.map();
		
		var values = "";
		for (String key : headers.keySet()) {
			values = "";
			for (String value : headers.get(key)) {
				values += " " + value;
			}
			Header header = new Header(key, values);
			httpResponse.addHeader( header );
		}
		
		httpResponse.setRequest( request );
		
		return httpResponse;
	}
	
	private void writeResponse(Socket socket, byte[] streamResponse, IHttpResponse response, boolean maliciousRequest, boolean maliciousResponse) {
		OutputStream outputStream = null;
		try {
			outputStream = socket.getOutputStream(); 
			
			if (maliciousRequest)
				outputStream.write("".getBytes());
			else {
				if (maliciousResponse && isAResponseValidForErrorPage( response )) {
					System.out.println("res: " + response.getRequest().getRequestedResource());
					IHttpErrorPage errorPage = new HttpErrorPageImpl("Description.", response.getHost());
					
					outputStream.write(errorPage.getStatusLine());
					outputStream.write(errorPage.getHeaders());
					outputStream.write(errorPage.getBody());
				}
				else {
					String firstLine = response.getStatusLine() + "\r\n";
					outputStream.write(firstLine.getBytes());
					for (Header header : response.getHeaders()) {
						String h = header.getKey() + ":" + header.getValues() + "\r\n";
						outputStream.write( h.getBytes() );
					}
					outputStream.write("\r\n".getBytes());
					
					outputStream.write(streamResponse);
				}
			}
			
			outputStream.flush();
			
			System.out.println("Thread " + Thread.currentThread().getName()+ " has finished.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Writing error...");
			e.printStackTrace();
		} finally {
			try {
            	if (!socket.isOutputShutdown()) {
            		socket.shutdownOutput();
            	}
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private boolean isAResponseValidForErrorPage(IHttpResponse response) {
		if(response.getRequest().getHeader("Referer") == null
			|| (response.getRequest().getHeader("Referer") != null && 
				response.getHeader("Content-type") != null && 
				response.getHeader("Content-type").getValues().contains("text/html"))) 
		{
			if (response.getHeader("Content-type").getValues().contains("text/html"))
				System.err.println("hasgdudafgidfygdfshaaaaaaaaaaaa " + response.getHeader("Content-type").getValues());
			return true;
		}
		
		return false;
	}
	
	
	/** Metodo que va leyendo caracter a caracter el contenido de la petici�n enviada al servidor, y lo va metiendo
	 * en un buffer. Si encuentra un car�cter que indica un fin de l�nea, termina de leer.
	 * Devuelve cadenas del siguiente estilo:
	 * 
     * CONNECT clients4.google.com:443 HTTP/1.1
     * Host: clients4.google.com:443
     * Proxy-Connection: keep-alive
     * User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.96 Safari/537.36
     * 
     * 
     * @return
     * @throws IOException en el caso en el que no se pueda leer del bufer
     */
	public IHttpRequest readRequest(InputStream in, boolean isSSL) {
		IHttpRequest request = new HttpRequestImpl();
		request.setSSL( isSSL );
		
		String headers="", line="line";
		do {
			if (line != "line")
				headers += "\r\n";
			line = readOneLine(in);
			if (line != null && !line.equals(""))
				headers += line;
			
		} while (line != null && !line.equals(""));
		
		request.buildRequest( headers );
		
		if (request.getMethod() != null && request.getHeader("Content-Length") != null) // We need to get the request body
		{
			int contentLength = Integer.parseInt( request.getHeader("Content-Length").getValues() );
			
			System.out.println("Method: " + request.getMethod() + ", content-length: " + contentLength);
			
			StringBuilder body = new StringBuilder();
	        int c = 0;
	        for (int i = 0; i < contentLength; i++) {
	        	
	            try {
					c = in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            body.append((char) c);
	        }
	        
//	        System.out.println( body.toString() );
	        request.setBody( body.toString().getBytes() );
		}
		
		return request;
	}
	
	private String readOneLine(InputStream in) {
		int characterRead;
		boolean endOfLine = false;
		ByteArrayOutputStream contentClientRequest = new ByteArrayOutputStream();
		
		try {
			while (( (characterRead = in.read()) != -1) && !endOfLine) {
				switch (characterRead)
				{
					case '\n': // si se encuentra un salto de linea, se entiende que estamos en el final de una linea
						endOfLine=true;
						break;
					case '\r': // idem que para \n
						endOfLine=true;
			            break;
			        default:
			        	contentClientRequest.write( characterRead );
			            break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
//		System.out.println( contentClientRequest.toString() );
		
		try {
			return contentClientRequest.toString("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	private void connectMethod( Socket socket, IHttpRequest request ) {
		
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
	
	private boolean configureRequest(IHttpOperation request) {
		IProxyFunctionality functionality = new ProxyFunctionalityImpl();
		functionality = new ModifyUserAgent(functionality);
		functionality = new CheckMaliciousHost(functionality);
		functionality = new CheckTrackerHost(functionality);
		functionality = new CheckPornographyHost(functionality);
		
		boolean maliciousRequest = false;
		IHttpOperation operation = functionality.modify( request );
		if (operation == null)
			maliciousRequest = true;
		
		return maliciousRequest;
	}
	
	private boolean configureResponse(IHttpOperation response) {
		IProxyFunctionality functionality = new ProxyFunctionalityImpl();
		functionality = new CheckSecurityHeaders(functionality);
		
		boolean maliciousResponse = false;
		IHttpOperation operation = functionality.modify( response );
		if (operation == null)
			maliciousResponse = true;
		
		return maliciousResponse;
	}
}
