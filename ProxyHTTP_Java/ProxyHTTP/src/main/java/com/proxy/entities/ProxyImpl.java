package com.proxy.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyImpl implements Proxy {

	public ProxyImpl() {}
	
	@Override
	public void connection() {
		ServerSocket serverSocket=null;
		try {
			serverSocket = new ServerSocket( ProxyConfig.getInstance().getPuertoLocal() );
		} catch (IOException e1) {
			// TODO: A un log.
			e1.printStackTrace();
		}
		
		Socket socket;
		Connection connectionCreator;
		try {
    		// Cuando haya una conexion abierta en el socket servidor, la abrimos/aceptamos y la manejamos.
            while ( (socket = serverSocket.accept() ) != null) {
            	
            	
            	/*
            	System.out.println("Primer round");
    			String aux = readRequest(socket);
    			while (!aux.equals(""))
    				aux = readRequest(socket);
            	
            	
            	// Send headers
                ObjectOutputStream wr =
                		new ObjectOutputStream(socket.getOutputStream());
                wr.writeObject("GET /secretaria/impresos HTTP/1.0\r\n");
                wr.writeObject("Host: ingenieriainformatica.uniovi.es\r\n");
                wr.writeObject("\r\n");
                wr.flush();
                
                System.out.println("Segundo round");
                aux = readRequest(socket);
    			while (!aux.equals(""))
    				aux = readRequest(socket);
    			*/
    			
                
            	
            	connectionCreator = new ConnectionImpl( new ClientConnectionImpl(socket) );
            	connectionCreator.start();
            }
        } catch (IOException e) {
        	// TODO: A un log.
            e.printStackTrace(); 
        }
	}
	
	
	
	
	private String readRequest(Socket socket) throws IOException {
		int characterRead;
		boolean endOfLine = false;
		ByteArrayOutputStream contentClientRequest = new ByteArrayOutputStream();
		
		while (( (characterRead = socket.getInputStream().read()) != -1) && !endOfLine) {
			switch (characterRead)
			{
				case '\n': // si se encuentra un salto de linea, se entiende que estamos en el final de una linea
					endOfLine=true;
					break;
				case '\r':
					endOfLine=true;
					break;
		        default:
		        	contentClientRequest.write( characterRead );
		            break;
			}
		}
		
		System.out.println( contentClientRequest.toString() );

		
		return contentClientRequest.toString( "ISO-8859-1" );
    }

}
