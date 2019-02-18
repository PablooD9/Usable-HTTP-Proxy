package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Clase encargada de crear un manejador de peticiones por cada petición recibida
 * @author Pablo
 *
 */
public class Proxy_Main extends Thread {
	
	private Proxy_Config configuradorProxy;
	private ServerSocket socketServidor;
	
	public Proxy_Main(Proxy_Config configuradorProxy) {
		this.configuradorProxy = configuradorProxy;
	}
	
	@Override
    public void run() {
    	try {
			socketServidor = new ServerSocket( configuradorProxy.getPuertoLocal() );
    	
			Socket socket;
	    	try {
	    		// Cuando haya una conexión abierta en el socket servidor, la abrimos/aceptamos y la manejamos.
	            while ( (socket = socketServidor.accept() ) != null) {
	                (new Proxy_Handler( socket )).start();
	            }
	        } catch (IOException e) {
	            e.printStackTrace(); 
	        }
    	} catch (IOException e1) {
			e1.printStackTrace();
		}
        
    }
	
}
