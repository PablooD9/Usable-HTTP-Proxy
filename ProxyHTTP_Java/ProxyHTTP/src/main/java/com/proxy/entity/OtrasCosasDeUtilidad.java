package com.proxy.entity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.proxy.entity.request.Header;
import com.proxy.entity.request.IHttpRequest;


public class OtrasCosasDeUtilidad {
	
	
	public static void main(String[] args) {
		/*HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .priority(1)
                .version(HttpClient.Version.HTTP_2)
				.build();
		
		var request = HttpRequest.newBuilder()  // GET request!
		        .uri(URI.create("http://ingenieriainformatica.uniovi.es/secretaria/impresos"))
		        .header("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36")
		        .GET()
		        .build();
		
		var response = client.sendAsync(request, BodyHandlers.ofString())
				.join();	
		
		HttpHeaders httpHeaders = response.headers();
		
		Map<String, List<String>> headers = httpHeaders.map();
//		headers.forEach((clave, valor) -> System.out.println( clave + "{ " + valor + "}" ));
            
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
//		System.out.println(protocol);
		
		int code = response.statusCode();
		System.err.println("Status code: "+code);
		
		String date = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
		// System.out.println(date);
		
		var crlf = "\r\n";
		var responseString = protocol + " " + code + " Sent. " + crlf;
		responseString += "Date: " + date + crlf;
		
		for (String key : headers.keySet()) {
			responseString += key + ":";
			for (String valor : headers.get(key)) {
				responseString += " " + valor;
			}
			responseString += crlf;
		}
		
		responseString += crlf; // espacio cabeceras y cuerpo

		responseString += response.body();
		
		System.out.println( responseString );
		*/
	}
	
	

	void manageConnection(ConnectionImpl conn) {
		
		/*
		OutputStream outstream;
		try {
			outstream = clientConn.getClientSocket().getOutputStream();
			
			PrintWriter out = new PrintWriter(outstream);

			out.print( responseString );
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
		
	}

	/*
	 
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
	  
	*/
	
	
	
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
