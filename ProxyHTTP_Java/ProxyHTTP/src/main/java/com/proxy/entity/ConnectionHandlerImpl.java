package com.proxy.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.HttpStatus;

import com.proxy.entity.request.Header;
import com.proxy.entity.request.HttpRequestImpl;
import com.proxy.entity.request.IHttpRequest;

public class ConnectionHandlerImpl implements ConnectionHandler {

	private final byte[] HTTP_OK = "HTTP/1.1 200 OK\r\n\r\n".getBytes();
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
					System.err.println(request.getHost());
					connectMethod( socket, request );
				}
				else {
					if (hostTarget == null)
						hostTarget = new InetSocketAddress(request.getHost(), request.getPort());

					obtainResponse(socket, request, null);
				}
				
				/*
					IProxyFunctionality functionality = new ProxyFunctionalityImpl();
					functionality = new ModifyUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)", functionality);
					functionality = new CheckMaliciousHost(functionality);
					functionality.modifyRequest( request );
				*/
				
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
	
	private void obtainResponse(Socket socket, IHttpRequest req, String uri) {

		HttpClient client = null;
		if (req.isSSL()) {
			SSLContext sslContext = ((SecureConnectionHandler)connHandler).createSSLContext( req.getHost() );
			client = HttpClient.newBuilder()
	                .connectTimeout(Duration.ofSeconds(30))
	                .priority(1)
	                .version(HttpClient.Version.HTTP_2)
	                .followRedirects(Redirect.NORMAL)
	                .sslContext( sslContext )
					.build();
		}
		else
			client = HttpClient.newBuilder()
		            .connectTimeout(Duration.ofSeconds(30))
		            .priority(1)
		            .version(HttpClient.Version.HTTP_2)
		            .followRedirects(Redirect.NORMAL)
		            .build();
		
		String protocolAndHost = ((req.isSSL()) ? "https://" : "http://") + req.getHost();
		
		if (uri == null)
			uri = protocolAndHost + req.getRequestedResource();
		else {
			if (uri.startsWith("/"))
				uri = protocolAndHost + uri;
			System.out.println("Aqui:" + uri);
		}
		
		HttpRequest.Builder preRequest=null;
		if (req.getMethod().equalsIgnoreCase("GET")) {
			preRequest = HttpRequest.newBuilder()  // GET request!
		        .uri(URI.create( uri ))
		        .GET();
		}
		else if (req.getMethod().equalsIgnoreCase("POST")) {
			preRequest = HttpRequest.newBuilder()  // POST request!
	        .uri(URI.create( uri ))
	        .POST(BodyPublishers.ofString(req.getBody()));
		}
		
		for (Header header : req.getHeaders()) {
			if (!header.getKey().equalsIgnoreCase("Host") &&
				!header.getKey().equalsIgnoreCase("Connection") &&
				!header.getKey().equalsIgnoreCase("Content-Length") &&
				!header.getKey().equalsIgnoreCase("Upgrade") ) 
			{
				preRequest.setHeader(header.getKey(), header.getValues());
			}
		}
		preRequest.header("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36");
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
			obtainResponse( socket, req, locationHeader.get() );
		}
		else {
			Map<String, List<String>> headers = httpHeaders.map();
//			headers.forEach((clave, valor) -> System.out.println( clave + "{ " + valor + "}" ));
	            
			String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
			protocol = protocol.replace("HTTP/2", "HTTP/1.1");
			System.out.println("proto: " + protocol);
			
			int code = response.statusCode();
//			System.err.println("Status code: "+ code + ", for METHOD::: " + req.getMethod());
			
			String reasonPhrase = HttpStatus.getStatusText( code );
			
			var crlf = "\r\n";
			
			var responseString = protocol + " " + code + " " + reasonPhrase + crlf;
			
			for (String key : headers.keySet()) {
				responseString += key + ":";
				for (String valor : headers.get(key)) {
					responseString += " " + valor;
				}
				responseString += crlf;
			}
			
			responseString += crlf; // espacio cabeceras y cuerpo

//			responseString += response.body();
			
			writeResponse(socket, response.body(), responseString);
		}
	}
	
	private void writeResponse(Socket socket, byte[] streamResponse, String responseHeaders) {
		OutputStream outputStream = null;
		try {
			outputStream = socket.getOutputStream(); 
			
			outputStream.write(responseHeaders.getBytes());
			outputStream.write(streamResponse);
			outputStream.flush();
			
			System.out.println("El hilo " + Thread.currentThread().getName()+ " ha acabado de escribir.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("NANIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
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
	
	private static void forwardData(Socket inputSocket, Socket outputSocket) {
        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(buffer);
                        if (read > 0) {
                            outputStream.write(buffer, 0, read);
                            if (inputStream.available() < 1) {
                                outputStream.flush();
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  // TODO: implement catch
        }
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
		
		request.parse( headers );
		
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
	        request.setBody( body.toString() );
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
	
}
