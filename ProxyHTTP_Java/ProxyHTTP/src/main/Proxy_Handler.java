package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/** Clase encargada de realizar las siguientes operaciones:
 * 		1. Recoger y procesar la informaci�n de la petici�n del cliente (navegador)
 * 		2. Enviar la petici�n del cliente al servidor
 * 		3. Recoger y procesar la informaci�n llegada del servidor
 * 		4. Enviar la respuesta del servidor al cliente
 * 
 * @author Pablo
 *
 */
public class Proxy_Handler extends Thread {

	private Socket socketCliente, socketServidor;
	private String URL = "";
	private String versionHTTP = "1.1";
	private int puertoServidor = 0;
	
	private final String ISO = "ISO-8859-1"; // Norma ISO correspondiente al alfabeto latino (�, �, �, etc.)
	
	public Proxy_Handler(Socket socket) {
		this.socketCliente = socket;
	}
	
	@Override
    public void run() {
    	try {
    		String URLYPuerto = recogePeticion();
    		
    		URL = URLYPuerto.split(":")[0];
    		puertoServidor = Integer.parseInt(URLYPuerto.split(":")[1]);
    		
    		if (versionHTTP != null && URL != null) {
    			try{
    				socketServidor = new Socket( URL, puertoServidor );
    			} catch (IOException | NumberFormatException e) {
                	System.out.print("Error: Host desconocido :>>>");
                	e.printStackTrace();  // TODO
                	escribeEncabezadoRespuesta( 505 );
                    return;
                }
    			try {
    				escribeEncabezadoRespuesta( 200 );
    				
    				enviarRespuesta();
    				
        		} finally 
    			{
	                socketServidor.close();
	            }
                
    		} // fin del if 
			
    	} catch (IOException e) {
    		System.out.print("Error [3]! :>>> ");
    		e.printStackTrace();
    	} finally {
            try {
                socketCliente.close();
            } catch (IOException e) {
            	System.out.print("Error [4]! :>>> ");
                e.printStackTrace();  // TODO
            }
        }
	}
	
	/** M�todo que recoge una petici�n desde el cliente, leyendo la primera l�nea y sacando de la misma la URL del servidor,
	 * el puerto remoto de dicha URL y la versi�n del protocolo HTTP.
	 * El formato de lo devuelto es:
	 * 
	 * CONNECT ab.blogs.es:443 HTTP/1.1
	 * 
	 * @return la URL y el puerto remoto
	 */
	private String recogePeticion() {
		String URLYPuerto = null;
		try {
			// Obtenemos la primera l�nea de la petici�n (obtenemos la URL, versi�n HTTP y puerto remoto)		
			String lineaPeticion = leerPeticion();
		
			versionHTTP = obtenerVersionHTTP( lineaPeticion );
			URLYPuerto = obtenerURL( lineaPeticion );
			
			if (URLYPuerto == null) {
				// throw new BusinessException(...);
				throw new IllegalStateException("Error en la obtenci�n de la URL y el puerto remoto");
			}
				
			// Una vez recogida la URL y el puerto, continuamos procesando las distintas cabeceras de la petici�n
			// hasta que no quede ninguna.
			String aux = leerPeticion();
			while (!aux.equals(""))
				aux = leerPeticion();
			
//			System.out.println( lineaPeticion );
			
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		
		return URLYPuerto;
	}
	
	/**
	 * << En futuras versiones, este m�todo ser� sustitu�do por un Factory. >>
	 * Devuelve encabezados HTTP, dependiendo del c�digo HTTP correspondiente.
	 */
	private void escribeEncabezadoRespuesta(int codigoError) {
		OutputStreamWriter streamWriterSalida;
		try {
			streamWriterSalida = new OutputStreamWriter(socketCliente.getOutputStream(), ISO);
			
			switch (codigoError) {
				case 200:
					streamWriterSalida.write(versionHTTP + " 200 Connection established\r\n");
//        			streamWriterSalida.write("Proxy-agent: Simple/0.1\r\n");
        			streamWriterSalida.write("\r\n");
        			streamWriterSalida.flush();
        			break;
				case 502:
					streamWriterSalida.write(versionHTTP + " 502 Bad Gateway\r\n");
//                  streamWriterSalida.write("Proxy-agent: Simple/0.1\r\n");
                    streamWriterSalida.write("\r\n");
                    streamWriterSalida.flush();
                    break;
			}
			
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		
	}
	
	/** M�todo que va leyendo caracter a caracter el contenido de la petici�n enviada al servidor, y lo va metiendo
	 * en un buffer. Si encuentra un car�cter que indica un fin de l�nea, termina de leer.
	 * Devuelve cadenas del siguiente estilo:
	 * 
     * CONNECT clients4.google.com:443 HTTP/1.1
     * Host: clients4.google.com:443
     * Proxy-Connection: keep-alive
     * User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.96 Safari/537.36
     * 
     * 
     * @param socket
     * @return
     * @throws IOException
     */
	/** 
	 * @return
	 * @throws IOException
	 */
	private String leerPeticion() throws IOException {
		int caracterLeido;
		boolean finDeLinea = false;
		ByteArrayOutputStream contenidoPeticionCliente = new ByteArrayOutputStream();
		
		while (( (caracterLeido = socketCliente.getInputStream().read()) != -1) && !finDeLinea) {
			switch (caracterLeido)
			{
				case '\n': // si se encuentra un salto de l�nea, se entiende que estamos en el final de una linea
					finDeLinea=true;
					break;
				case '\r': // idem que para \n
		            finDeLinea=true;
		            break;
		        default:
		        	contenidoPeticionCliente.write( caracterLeido );
		            break;
			}
		}
		
		System.out.println( contenidoPeticionCliente.toString() );
		
		return contenidoPeticionCliente.toString( ISO ); // Lo devolvemos en formato ISO-8859-1
    }
	
	private Thread enviarRespuesta() {
		// Manejamos el env�o del contenido de la p�gina web al navegador en otro hilo
		Thread servidorACliente = new Thread() {
            @Override
            public void run() {
                enviarDatosServidorACliente( socketServidor, socketCliente );
            }
        };
        servidorACliente.start();
        
        try {
            int read = socketCliente.getInputStream().read();
            
            // Si no falta por leer informaci�n enviada desde el navegador, m�todo read() devolver� -1.
            // En otro caso, 
            if (read != -1) {
                if (read != '\n') {
                    socketServidor.getOutputStream().write(read);
                }
                enviarDatosServidorACliente(socketCliente, socketServidor);
            } else {
               /* if (!socketServidor.isOutputShutdown()) {
                    socketServidor.shutdownOutput();
                }
                if (!socketCliente.isInputShutdown()) {
                    socketCliente.shutdownInput();
                }
               */
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally 
        {
            try {
            	if (!socketServidor.isOutputShutdown()) {
                    socketServidor.shutdownOutput();
                }
                if (!socketCliente.isInputShutdown()) {
                    socketCliente.shutdownInput();
                }
                
                servidorACliente.join();
            } catch (InterruptedException e) {
                System.out.println("Error [2]! :>>> ");
              	e.printStackTrace();  // TODO:
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
        
        return servidorACliente;
	}
	
	private String obtenerURL(String cadenaPeticion) {
		return cadenaPeticion.contains("CONNECT") ? cadenaPeticion.split(" ")[1] : null;
	}
	
	private String obtenerVersionHTTP(String cadenaPeticion) {
		return cadenaPeticion.contains("CONNECT") ? cadenaPeticion.split(" ")[2] : null;
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
            } finally { // Cerramos los canales de comunicaci�n (socket cliente y servidor)
                /*if (!socketEntrada.isInputShutdown()) {
                    socketEntrada.shutdownInput();
                }
                
                if (!socketSalida.isOutputShutdown()) {
                    socketSalida.shutdownOutput();
                }
                */
            }
            
        } catch (IOException e) {
        	System.out.print("Error [5]! :>>> ");
            e.printStackTrace();
        }
	}
}
