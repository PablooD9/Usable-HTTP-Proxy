package com.proxy.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.proxy.entity.io.ChunkedInputStream;
import com.proxy.entity.io.CopyInputStream;
import com.proxy.entity.io.FixedLengthInputStream;
import com.proxy.entity.request.Header;
import com.proxy.entity.request.HttpRequestImpl;
import com.proxy.entity.request.IHttpRequest;

public class ConnectionHandlerImpl implements ConnectionHandler {

	private final byte[] HTTP_OK = "HTTP/1.1 200 OK\r\n\r\n".getBytes();
	private ConnectionHandler connHandler; // useful for SSL communications
	
	public ConnectionHandlerImpl(SecureConnectionHandler connHandler) {
		setConnectionHandler(connHandler);
	}

	public void setConnectionHandler(ConnectionHandler connHandler) {
		this.connHandler = connHandler;
	}
	
	@Override
	public void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConnection) {
		
    	System.out.println( "=========================== Starting new request. THREAD: " + Thread.currentThread().getName() + " =============================");
		InputStream inStream;
		try {
			inStream = socket.getInputStream();
			
			boolean closeConnection = false;
			int count = 0;
//			while (!closeConnection) {
				IHttpRequest request = null;
				request = readRequest(inStream, sslConnection);
				/*
				if (request == null)
				{
					return ;
				}
					
				if (request.getMethod().equalsIgnoreCase("CONNECT")) { // SSL Connection? Probably.
					connectMethod( socket, request );
					return ;
				}
				if (hostTarget == null)
					hostTarget = new InetSocketAddress(request.getHost(), request.getPort());
				*/
//			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
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
	private IHttpRequest readRequest(InputStream in, boolean isSSL) {
		IHttpRequest request = new HttpRequestImpl();
		request.setSSL( isSSL );
		
		String headers="", line="line";
		do {
			
			line = readOneLine(in);
			if (line != null && !line.equals(""))
				headers += line + "\r\n";
			
		} while (line != null && !line.equals(""));
		
		request.parse( headers );
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
	 * 
	 * 
	 * http://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed.html
	 * 
	 * 
	 * 
	 * */
	
	
	
	
	private String sendResponse(Socket clientSocket, IHttpRequest request, InetSocketAddress hostTarget) {
		
		HttpClient client = null;
		if (request.isSSL()) {
			SSLContext sslContext = ((SecureConnectionHandler)connHandler).createSSLContext(request.getHost());
			
			client = HttpClient.newBuilder()
			        .connectTimeout(Duration.ofSeconds(60))
			        .priority(1)
			        .version(HttpClient.Version.HTTP_2)
			        .sslContext(sslContext)
					.build();
		}
		else {
			client = HttpClient.newBuilder()
			        .connectTimeout(Duration.ofSeconds(60))
			        .priority(1)
			        .version(HttpClient.Version.HTTP_2)
					.build();
		}
			
		String URItarget = ((request.isSSL()) ? "https://" : "http://") + request.getHost() + request.getRequestedResource();
		System.out.println("REQUEST TO: " + URItarget + ", method " + request.getMethod() + ", Thread::::> " + Thread.currentThread().getName());
			
		HttpRequest.Builder httpReqBuild = HttpRequest.newBuilder()
							.uri(URI.create(URItarget))
							.timeout(Duration.ofSeconds(60))
//							.setHeader("User-Agent", request.getHeader("User-agent").getValues())
							.GET();
		
		/*for (Header header : request.getHeaders()) {
			if (!header.getKey().equalsIgnoreCase("upgrade") && !header.getKey().equalsIgnoreCase("host") && !header.getKey().equalsIgnoreCase("connection") && !header.getKey().equalsIgnoreCase("origin"))
			httpReqBuild.header(header.getKey(), header.getValues());
		}*/
		
		HttpRequest httpReq = httpReqBuild.build();
		/*
							HttpHeaders headers = httpReq.headers();
							Map<String, List<String>> map = headers.map();
							var a = Arrays.toString(map.entrySet().toArray());
							System.err.println( a );
		*/
		
		HttpResponse<String> response=null;
		try {
			response = client.sendAsync(httpReq, BodyHandlers.ofString())
								.join();	
		} catch (CompletionException ce)
		{
			// TODO
//			ce.printStackTrace();
			return null;
		}

		HttpHeaders httpHeaders = response.headers();
		
		Map<String, List<String>> headersResponse = httpHeaders.map();
//		headersResponse.forEach((clave, valor) -> System.out.println( clave + "{ " + valor + " }" ));
		
		
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
//		System.out.println(protocol);
		
		int code = response.statusCode();
		System.err.println("Status code: "+code);
		
		String date = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
		// System.out.println(date);
		
		var crlf = "\r\n";
		var responseString = protocol + " " + code + " OK " + crlf;
		responseString += "Date: " + date + crlf;
		
		for (String key : headersResponse.keySet()) {
			responseString += key + ":";
			for (String valor : headersResponse.get(key)) {
				responseString += " " + valor;
			}
			responseString += crlf;
		}
		
		responseString += crlf; // White space between headers and body

		responseString += response.body();
		
//		System.out.println( responseString );
		
		return responseString;
	}
	
	private void writeResponseInClient(Socket clientSocket, String response) {
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(clientSocket.getOutputStream());
			printWriter.write(response + "\r\n");
	        printWriter.flush();
//	        printWriter.close();
	        
	        System.err.println("<============ RESPUESTA ENVIADA ================>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	private void pruebas(Socket clientSocket, IHttpRequest request, InetSocketAddress hostTarget) {
		
		CloseableHttpResponse httpResponse=null;
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet get = new HttpGet(((request.isSSL()) ? "https://" : "http://") + request.getHost() + request.getRequestedResource());
			System.out.println(get.getURI());
			httpResponse = httpClient.execute(get);
			
			    System.out.println(httpResponse.getStatusLine());
			    HttpEntity entity1 = httpResponse.getEntity();
			    // do something useful with the response body
			    // and ensure it is fully consumed
			    EntityUtils.consume(entity1);
			    
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (httpResponse != null)
					httpResponse.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
