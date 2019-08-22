package com.proxy.entity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.HttpStatus;

import com.proxy.entity.request.Header;
import com.proxy.entity.request.HttpRequestImpl;
import com.proxy.entity.request.IHttpRequest;
import com.proxy.model.functionality.CheckMaliciousHost;
import com.proxy.model.functionality.IProxyFunctionality;
import com.proxy.model.functionality.ModifyUserAgent;
import com.proxy.model.functionality.ProxyFunctionalityImpl;

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
		try {
			inStream = socket.getInputStream();
			
			IHttpRequest request = null;
			request = readRequest(inStream, sslConnection);
			
			if (request.getHeaders().isEmpty()){
				closeSocket( socket );
				return ;
			}
			
			if (request.getMethod().equalsIgnoreCase("CONNECT")) { // SSL Connection? Probably.
				connectMethod( socket, request );
				return ;
			}
			
			IProxyFunctionality functionality = new ProxyFunctionalityImpl();
			functionality = new ModifyUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)", functionality);
			functionality = new CheckMaliciousHost(functionality);
			functionality.modifyRequest( request );
			
			
			if (hostTarget == null)
				hostTarget = new InetSocketAddress(request.getHost(), request.getPort());

			InputStream responseStream = obtainResponse(request, null);
			if (responseStream != null)
				writeResponse(socket, responseStream);
			
			closeSocket( socket );

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void closeSocket(Socket socket) {
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException ignore) {}
	}
	
	private InputStream obtainResponse(IHttpRequest req, String uri) {
	
		HttpClient client = null;
		if (req.isSSL()) {
			SSLContext sslContext = ((SecureConnectionHandler)connHandler).createSSLContext( req.getHost() );
			
			client = HttpClient.newBuilder()
	                .connectTimeout(Duration.ofSeconds(30))
	                .priority(1)
	                .version(HttpClient.Version.HTTP_2)
	                .sslContext( sslContext )
					.build();
		}
		else
			client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .priority(1)
            .version(HttpClient.Version.HTTP_2)
			.build();
		
		if (uri == null)
			uri = ((req.isSSL()) ? "https://" : "http://") + req.getHost() + req.getRequestedResource();
		
		HttpRequest request=null;
		if (req.getMethod().equalsIgnoreCase("GET")) {
			request = HttpRequest.newBuilder()  // GET request!
		        .uri(URI.create( uri ))
		        .GET()
		        .build();
		}
		else if (req.getMethod().equalsIgnoreCase("POST")) {
			request = HttpRequest.newBuilder()  // POST request!
	        .uri(URI.create( uri ))
	        .POST(BodyPublishers.ofString(req.getBody()))
	        .build();
		}
			
		HttpResponse<String> response;
		try {
			response = client.sendAsync(request, BodyHandlers.ofString())
				.join();	
		} catch (CompletionException ce) {
			System.err.println("Address " + uri + " is unreachable!");
			return null;
		}
		
		HttpHeaders httpHeaders = response.headers();
		
		Optional<String> locationHeader = httpHeaders.firstValue("Location"); // When resource has been permanently moved
		
		if ( !locationHeader.isEmpty() ) {
			System.out.println("Moved permanently to " + locationHeader.get());
			return obtainResponse( req, locationHeader.get() );
		}
		
		Map<String, List<String>> headers = httpHeaders.map();
//		headers.forEach((clave, valor) -> System.out.println( clave + "{ " + valor + "}" ));
            
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
//		System.out.println(protocol);
		
		int code = response.statusCode();
		System.err.println("Status code: "+ code + ", for METHOD::: " + req.getMethod());
		
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

		responseString += response.body();
		
		InputStream stream = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));
		
		return stream;
	}
	
	private void writeResponse(Socket socket, InputStream streamResponse) {
		try {
			OutputStream outputStream = socket .getOutputStream(); 
			byte[] buffer = new byte[4096];
            int read;
            do {
                read = streamResponse.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                    if (streamResponse.available() < 1) {
                        outputStream.flush();
                    }
                }
            } while (read >= 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendResponse(IHttpRequest request) {
		String urlFormatted = ((request.isSSL()) ? "https://" : "http://") + request.getHost() + request.getRequestedResource();
		startConnection( urlFormatted, request );
	}
	
	
	private void startConnection(String urlFormatted, IHttpRequest request) {
		URL url=null;
		try {
			url = new URL( urlFormatted );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (url.getProtocol().equalsIgnoreCase("http")) {
			try {
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				
				con.setRequestMethod( request.getMethod() );
				
				for (Header header : request.getHeaders()) {
					con.setRequestProperty(header.getKey(), header.getValues());
				}
				
				if (request.getMethod().equalsIgnoreCase("POST") 
						|| request.getMethod().equalsIgnoreCase("PUT")
						|| request.getMethod().equalsIgnoreCase("PATCH"))
				{
					con.setDoOutput( true );
					con.setDoInput(true);
	
					DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
					wr.write( request.getBody().getBytes() );
					
					System.err.println("CUERPO ENVIADO!!!");
				}
				
				int status = -1;
				Reader streamReader = null;
			try {
				status = con.getResponseCode();
				 
				if (status > 299 || status == -1) {
				    streamReader = new InputStreamReader(con.getErrorStream());
				} else {
				    streamReader = new InputStreamReader(con.getInputStream());
				}
			} catch ( SocketException se) {
				System.err.println("SSL? " + request.isSSL() + ", método: " + request.getMethod() + ", URL: " + urlFormatted);
				return ;
			}
			
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						  || status == HttpURLConnection.HTTP_MOVED_PERM)
				{
				    String location = con.getHeaderField("Location");
				    startConnection(location, request);
				    return ;
				}
				
				byte[] fullResponse = getFullResponse(con, request, streamReader);
				writeBytes(fullResponse, socket);
				
				con.disconnect();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (url.getProtocol().equalsIgnoreCase("https")) {
			try {
				SSLContext sc = ((SecureConnectionHandler)connHandler).createSSLContext( request.getHost() );
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setSSLSocketFactory(sc.getSocketFactory());
				
				con.setRequestMethod( request.getMethod() );
				
				for (Header header : request.getHeaders()) {
					con.setRequestProperty(header.getKey(), header.getValues());
				}
				
				if (request.getMethod().equalsIgnoreCase("POST") 
						|| request.getMethod().equalsIgnoreCase("PUT")
						|| request.getMethod().equalsIgnoreCase("PATCH"))
				{
					con.setDoOutput( true );
					con.setDoInput(true);
	
					DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
					wr.write( request.getBody().getBytes() );
					
					System.err.println("CUERPO ENVIADO!!!");
				}
				
				int status = -1;
				Reader streamReader = null;
			try {
				status = con.getResponseCode();
				 
				if (status > 299 || status == -1) {
				    streamReader = new InputStreamReader(con.getErrorStream());
				} else {
				    streamReader = new InputStreamReader(con.getInputStream());
				}
			} catch ( SocketException se) {
				System.err.println("SSL? " + request.isSSL() + ", método: " + request.getMethod() + ", URL: " + urlFormatted);
				return ;
			}
			
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						  || status == HttpURLConnection.HTTP_MOVED_PERM)
				{
				    String location = con.getHeaderField("Location");
				    startConnection(location, request);
				    return ;
				}
				
				byte[] fullResponse = getFullResponse(con, request, streamReader);
				writeBytes(fullResponse, socket);
				
				con.disconnect();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public byte[] getFullResponse(HttpURLConnection con, IHttpRequest request, Reader streamReader) {
        StringBuilder fullResponseBuilder = new StringBuilder();
 
        // read status and message
        try {
			fullResponseBuilder.append(request.getHttpVersion())
			.append(" ")
			.append(con.getResponseCode())
			.append(" ")
			.append(con.getResponseMessage())
			.append("\r\n");
 
	        // read headers
	        con.getHeaderFields().entrySet().stream()
	        .filter(entry -> entry.getKey() != null)
	        .forEach(entry -> {
	            fullResponseBuilder.append(entry.getKey()).append(": ");
	            List<String> headerValues = entry.getValue();
	            Iterator<String> it = headerValues.iterator();
	            if (it.hasNext()) {
	                fullResponseBuilder.append(it.next());
	                while (it.hasNext()) {
	                    fullResponseBuilder.append(", ").append(it.next());
	                }
	            }
	            fullResponseBuilder.append("\r\n");
	        });
	        fullResponseBuilder.append("\r\n");
	        
	        // read response content
	        BufferedReader in = new BufferedReader( streamReader );
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				fullResponseBuilder.append(inputLine);
			}
			in.close();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return fullResponseBuilder.toString().getBytes();
    }
	
	private void writeBytes(byte[] bytesToWrite, Socket socket) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			
			try {
                outputStream.write( bytesToWrite );
                outputStream.flush();
            } finally {
                if (!socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		if (request.getMethod() != null && (request.getMethod().equalsIgnoreCase("post") 
				|| request.getMethod().equalsIgnoreCase("put") 
				|| request.getMethod().equalsIgnoreCase("patch"))) // We need to get the request body
		{
			int contentLength = Integer.parseInt( request.getHeader("Content-Length").getValues() );
			
			System.out.println("Método: " + request.getMethod() + ", content-length: " + contentLength);
			
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
	        
	        System.out.println( body.toString() );
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
	
	
	
	
	
	
	
	/*
	 * 			==== OTRAS COSAS ====
	 * */
	
	/*
	 
	 	OutputStreamWriter outputStreamWriter = null;
		final Socket forwardSocket;
        try {
        	outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(),
                    "ISO-8859-1");
        	
            forwardSocket = new Socket(request.getHost(), request.getPort());

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();  // TODO: implement catch
            try {
            	outputStreamWriter.write(request.getHttpVersion() + " 502 Bad Gateway\r\n");
				outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
				outputStreamWriter.write("\r\n");
	            outputStreamWriter.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            return;
        }
        try {
            try {
				outputStreamWriter.write(request.getHttpVersion() + " 200 Connection established\r\n");
				outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
	            outputStreamWriter.write("\r\n");
	            outputStreamWriter.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            

            Thread remoteToClient = new Thread() {
                @Override
                public void run() {
                    forwardData(forwardSocket, socket);
                }
            };
            remoteToClient.start();
            try {
                int read = socket.getInputStream().read();
                if (read != -1) {
                    if (read != '\n') {
                        forwardSocket.getOutputStream().write(read);
                    }
                    forwardData(socket, forwardSocket);
                } else {
                    if (!forwardSocket.isOutputShutdown()) {
                        forwardSocket.shutdownOutput();
                    }
                    if (!socket.isInputShutdown()) {
                        socket.shutdownInput();
                    }
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                try {
                    remoteToClient.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();  // TODO: implement catch
                }
            }
        } finally {
            try {
				forwardSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	 
	 */
	
	
	
	
	
	/*
	public IHttpRequest readRequest(InputStream in, boolean sslConnection) throws IOException {

		IHttpRequest request = new HttpRequestImpl();
		request.setSSL( sslConnection );
		
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
	*/
}
