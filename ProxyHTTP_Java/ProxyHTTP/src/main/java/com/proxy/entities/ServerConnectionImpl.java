package com.proxy.entities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerConnectionImpl extends Connection {

	private Socket serverSocket;
	private ClientConnection clientConn;
	
	@Override
	void manageConnection(Connection conn) {
		this.clientConn = (ClientConnection) conn;
		
		if ( clientConn.connectionIsValid() ) {
			try{
				serverSocket = new Socket( clientConn.getHostURL(), clientConn.getServerPort() );
			} catch (NumberFormatException nfe) {
				System.err.println("Error: Impossible to connect (Unknown Server Port) :>>> " + clientConn.getServerPort());
				// TODO Almacenar error en log
		    	return ;
			} catch (UnknownHostException uhe) {
				try {
					serverSocket = new Socket( "https://" + clientConn.getHostURL(), clientConn.getServerPort() );
				} catch (UnknownHostException e) {
					System.err.println("Error: Impossible to connect (Unknown Host) :>>> " + clientConn.getHostURL());
					e.printStackTrace();
					return ;
				} catch (IOException e) {
					// TODO Almacenar error en log
					e.printStackTrace();
				}
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
                    
                    if (bytes_leidos > 0) { // Mientras haya contenido que escribir
                        streamSalida.write(buffer, 0, bytes_leidos); // Pasamos el contenido del buffer al stream
                        if (streamEntrada.available() < 1) {
                            streamSalida.flush();
                        }
                    }
                }
                // ISO-8859-15
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
			streamWriterSalida.write("\r\n");
			streamWriterSalida.flush();
			
		} catch (IOException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		}
		
	}

}
