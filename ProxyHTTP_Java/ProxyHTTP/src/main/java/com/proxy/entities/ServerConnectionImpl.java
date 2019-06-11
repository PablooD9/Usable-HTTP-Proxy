package com.proxy.entities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ServerConnectionImpl extends Connection {

	private Socket serverSocket;
	private ClientConnection clientConn;
	
	@Override
	void manageConnection(Connection conn) {
		/*
		this.clientConn = (ClientConnection) conn;
		
		HttpClient client = HttpClient.newBuilder()
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
            
		/* protocol ->  */
		/*
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
//		System.out.println(protocol);
		
		int code = response.statusCode();
		
		String date = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
		// System.out.println(date);
		
		
		var crlf = "\r\n";
		var responseString = protocol + " 200 OK" + crlf;
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
		
		
		
		
		
		
		this.clientConn = (ClientConnection) conn;
		
		if ( clientConn.connectionIsValid() ) {
			try{
				serverSocket = new Socket( clientConn.getHost(), clientConn.getServerPort() );
			} catch (NumberFormatException nfe) {
				System.err.println("Error: Impossible to connect (Unknown Server Port) :>>> " + clientConn.getServerPort());
				// TODO Almacenar error en log
		    	return ;
			} catch (UnknownHostException uhe) {
				System.err.println("Error: Impossible to connect (Unknown Host) :>>> " + clientConn.getHost());
				System.out.println( clientConn.getServerPort() );
				uhe.printStackTrace();
				return ;
		    } catch (IOException e) {
		    	// TODO Almacenar error en log
				e.printStackTrace();
			}
			
			writeResponseHeader();
			sendResponse();
		
			try {
				if (!serverSocket.isClosed())
					serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
//			System.out.println( "HOST(2): " + getHostURL() );

		}
		
	}
	
	private Thread sendResponse() {
		// Manejamos el env�o del contenido de la p�gina web al navegador en otro hilo
		Thread servidorACliente = new Thread() {
            @Override
            public void run() {
                enviarDatosServidorACliente( serverSocket, clientConn.getClientSocket() );
            }
        };
        servidorACliente.start();
        
        try {
            int read = clientConn.getClientSocket().getInputStream().read();
            // Si no falta por leer informaci�n enviada desde el servidor, m�todo read() devolvera -1.
            if (read != -1) {
            	serverSocket.getOutputStream().write(read);
                enviarDatosServidorACliente( clientConn.getClientSocket(), serverSocket);
            } else {
               if (!serverSocket.isOutputShutdown()) {
            	   serverSocket.shutdownOutput();
                }
                if (!serverSocket.isInputShutdown()) {
                	serverSocket.shutdownInput();
                }
            }
            
        } catch (IOException e) {
        	System.err.println("Error: Couldn't read from the buffer :>>> ");
        	// TODO Almacenar error en log
			e.printStackTrace();
		} finally 
        {
            try {
            	if (!serverSocket.isOutputShutdown()) {
            		serverSocket.shutdownOutput();
                }
                if (!serverSocket.isInputShutdown()) {
                	serverSocket.shutdownInput();
                }
                
                servidorACliente.join();
            } catch (InterruptedException e) {
            	// TODO Almacenar error en log
              	e.printStackTrace();
            } catch (IOException e) {
            	// TODO Almacenar error en log
				e.printStackTrace();
			}
        } 
        
        return servidorACliente;
	}
	
	/** M�todo que escribe bytes desde un socket de entrada hacia un socket de salida.
	 * Es usado para enviar las respuestas del servidor al cliente.
	 * Los pasos que sigue este m�todo son los siguientes:
	 * 1� - Obtenemos los streams ("corrientes de datos") de los sockets, necesarios para
	 * pasarse la informaci�n entre los mismos.
	 * 2� - Mientras hayan bytes pendientes de pasar en el socket de entrada, escribimos los bytes desde este
	 * hasta el de salida.
	 * @param socketEntrada es el canal que contiene los datos a pasar
	 * @param socketSalida es el canal de comunicaci�n que recibe datos
	 */
	private void enviarDatosServidorACliente( Socket socketEntrada, Socket socketSalida ) {
		try {
            InputStream streamEntrada = socketEntrada.getInputStream();

            try {
            	
                OutputStream streamSalida = socketSalida.getOutputStream();
                int tamanio_buffer = 4096;
                byte[] buffer = new byte[ tamanio_buffer ];
                int bytes_leidos = 0;
                
                while (bytes_leidos >= 0) {
                	// Almacenamos lo le�do en el buffer (temporalmente)
                    bytes_leidos = streamEntrada.read(buffer);
                    char a = (char) bytes_leidos;
                    
                    if (bytes_leidos > 0) { // Mientras haya contenido que escribir
                        streamSalida.write(buffer, 0, bytes_leidos); // Pasamos el contenido del buffer al stream
                        if (streamEntrada.available() < 1) {
                            streamSalida.flush();
                        }
                    }
                }
                
            	
            } finally { // Cerramos los canales de comunicaci�n (socket cliente y servidor)
                if (!socketEntrada.isInputShutdown()) {
                    socketEntrada.shutdownInput();
                }
                
                if (!socketSalida.isOutputShutdown()) {
                    socketSalida.shutdownOutput();
                }
                
            }
            
        } catch (IOException e) {
        	System.out.print("Error [5]! :>>> ");
        	// TODO Almacenar error en log
            e.printStackTrace();
        }
	}
	
	
	/**
	 * Devuelve el encabezado OK del protocolo HTTP (código 200)
	 */
	private void writeResponseHeader() {
		
		OutputStreamWriter streamWriterSalida;
		try {
			streamWriterSalida = new OutputStreamWriter(clientConn.getClientSocket().getOutputStream(), getISO());
			
			streamWriterSalida.write( clientConn.getHTTPVersion() + " 200\r\n");
			streamWriterSalida.write("Proxy-agent: Simple/0.1\r\n");
			streamWriterSalida.write("User-Agent: Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36\r\n");
			streamWriterSalida.write("\r\n");
			streamWriterSalida.flush();
			
		} catch (IOException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void enviarDatosServidorACliente( byte[] bytes, Socket socketSalida ) {
		try {
            try {
            	
                OutputStream streamSalida = socketSalida.getOutputStream();
                
                streamSalida.write( bytes );
                streamSalida.flush();
                
            	
            } finally { // Cerramos los canales de comunicaci�n (socket cliente y servidor)
                
                if (!socketSalida.isOutputShutdown()) {
                    socketSalida.shutdownOutput();
                }
                
            }
            
        } catch (IOException e) {
        	System.out.print("Error [5]! :>>> ");
        	// TODO Almacenar error en log
            e.printStackTrace();
        }
	}
	
	
	

}
