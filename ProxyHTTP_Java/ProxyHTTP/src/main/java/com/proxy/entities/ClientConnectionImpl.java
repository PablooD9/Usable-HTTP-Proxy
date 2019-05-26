package com.proxy.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl extends ClientConnection {

	public ClientConnectionImpl(Socket clientSocket) {
		super( clientSocket );
	}
	
	@Override
	void manageConnection(Connection conn) {
		sendRequest();
		
		try {
			if ( getClientSocket().isClosed() )
				getClientSocket().close();
		} catch (IOException e) {
			// TODO Almacenar en LOG
			e.printStackTrace();
		}
	}
	
	@Override
	void sendRequest() {
		String URLandPort = null;
		try {
			// Obtenemos la primera linea de la peticion (obtenemos la URL, version HTTP y puerto remoto)		
			String requestLine = readRequest();
		
			setHTTPVersion( obtenerVersionHTTP( requestLine ) );

			URLandPort = obtenerURL( requestLine );
			if (URLandPort == null) {
				throw new IllegalStateException("Error getting URL and remote port");
			}
			
			
			
			/*
			
			if (URLandPort.contains(":")) {
				this.setHostURL( URLandPort.split(":")[0] );
				setServerPort(Integer.parseInt( URLandPort.split(":")[1] ));
				
				// Una vez recogida la URL y el puerto, continuamos procesando las distintas 
				// cabeceras de la petici�n hasta que no quede ninguna.
				String aux = readRequest();
				while (!aux.equals(""))
					aux = readRequest();
			}
			
			else // Va por HTTP
			{
				this.setHostURL( URLandPort.split(":")[0] );
				setServerPort( 80 );
			}
			
			*/
			
		} catch (IOException e) {
			System.err.println("Error X :>>> ");
			// TODO Almacenar error en log
			e.printStackTrace();
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
	private String readRequest() throws IOException {
		int characterRead;
		boolean endOfLine = false;
		ByteArrayOutputStream contentClientRequest = new ByteArrayOutputStream();
		
		while (( (characterRead = getClientSocket().getInputStream().read()) != -1) && !endOfLine) {
			switch (characterRead)
			{
				case '\n': // si se encuentra un salto de l�nea, se entiende que estamos en el final de una linea
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
		
		System.out.println( contentClientRequest.toString() );
		
		return contentClientRequest.toString( getISO() ); // Lo devolvemos en formato ISO-8859-1
    }
	
	/** MOVER A UNA CLASE
	 * @param requestStr
	 * @return
	 */
	private String obtenerURL(String requestStr) {
		if (requestStr.contains("CONNECT ") ||
				requestStr.contains("POST ") ||
					requestStr.contains("GET "))
		{
			return requestStr.split(" ")[1].replaceAll("http[s]*://", "");
		}
		
		System.out.println("esto: " + requestStr);
		
		return null;
	}
	
	/** MOVER A UNA CLASE
	 * @param requestStr
	 * @return
	 */
	private String obtenerVersionHTTP(String requestStr) {
		if (requestStr.contains("CONNECT ") ||
				requestStr.contains("POST ") ||
					requestStr.contains("GET "))
		{
			return requestStr.split(" ")[2];
		}
		
		return null;
	}

}
