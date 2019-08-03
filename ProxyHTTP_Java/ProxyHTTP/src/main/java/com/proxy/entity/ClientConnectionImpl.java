package com.proxy.entity;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl {

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
		throw new IllegalStateException("Error getting URL and remote port");
    }
	
	/** MOVER A UNA CLASE
	 * @param requestStr
	 * @return
	 */
	private String obtainURL(String requestStr) {
		throw new IllegalStateException("Error getting URL and remote port");
	}
	
	/** MOVER A UNA CLASE
	 * @param requestStr
	 * @return
	 */
	private String obtainHTTPVersion(String requestStr) {
		throw new IllegalStateException("Error getting URL and remote port");
	}

}
