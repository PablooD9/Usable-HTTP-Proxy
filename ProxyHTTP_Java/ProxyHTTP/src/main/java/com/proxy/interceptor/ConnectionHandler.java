package com.proxy.interceptor;

import java.net.InetSocketAddress;
import java.net.Socket;

/** Interfaz que define las operaciones necesarias para el manejo de las conexiones
 * a partir de la información obtenida de los Sockets.
 * @author Pablo
 *
 */
public interface ConnectionHandler extends Runnable {
	/** Método que, a partir de la información obtenida de un Socket, construye y configura 
	 * una petición para, finalmente, construir una respuesta y escribirla en el Socket.
	 * @param socket Socket para leer/escribir información.
	 * @param hostTarget Host al que va dirigida una petición.
	 * @param sslConnection Parámetro que permite distinguir si una petición va hacia un Host que implementa
	 * HTTPS o no.
	 */
	void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConnection);
	
	/** Establece una referencia a un objeto de tipo ConnectionHandler.
	 * @param connHandler Objeto de tipo ConnectionHandler.
	 */
	void setConnectionHandler(ConnectionHandler connHandler);
}
