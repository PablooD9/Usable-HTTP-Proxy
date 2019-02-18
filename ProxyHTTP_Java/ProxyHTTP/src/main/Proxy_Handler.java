package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/** Clase encargada de realizar las siguientes operaciones:
 * 		1. Recoger y procesar la información de la petición del cliente (navegador)
 * 		2. Enviar la petición del cliente al servidor
 * 		3. Recoger y procesar la información llegada del servidor
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
	
	private final String ISO = "ISO-8859-1"; // Norma ISO correspondiente al alfabeto latino (ñ, á, ç, etc.)
	
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
	
	/** Método que recoge una petición desde el cliente, leyendo la primera línea y sacando de la misma la URL del servidor,
	 * el puerto remoto de dicha URL y la versión del protocolo HTTP.
	 * El formato de lo devuelto es:
	 * 
	 * CONNECT ab.blogs.es:443 HTTP/1.1
	 * 
	 * @return la URL y el puerto remoto
	 */
	private String recogePeticion() {
		String URLYPuerto = null;
		try {
			// Obtenemos la primera línea de la petición (obtenemos la URL, versión HTTP y puerto remoto)		
			String lineaPeticion = leerPeticion();
		
			versionHTTP = obtenerVersionHTTP( lineaPeticion );
			URLYPuerto = obtenerURL( lineaPeticion );
			
			if (URLYPuerto == null) {
				// throw new BusinessException(...);
				throw new IllegalStateException("Error en la obtención de la URL y el puerto remoto");
			}
				
			// Una vez recogida la URL y el puerto, continuamos procesando las distintas cabeceras de la petición
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
	 * << En futuras versiones, este método será sustituído por un Factory. >>
	 * Devuelve encabezados HTTP, dependiendo del código HTTP correspondiente.
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
	
	/** Método que va leyendo caracter a caracter el contenido de la petición enviada al servidor, y lo va metiendo
	 * en un buffer. Si encuentra un carácter que indica un fin de línea, termina de leer.
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
				case '\n': // si se encuentra un salto de línea, se entiende que estamos en el final de una linea
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
		// Manejamos el envío del contenido de la página web al navegador en otro hilo
		Thread servidorACliente = new Thread() {
            @Override
            public void run() {
                enviarDatosServidorACliente( socketServidor, socketCliente );
            }
        };
        servidorACliente.start();
        
        try {
            int read = socketCliente.getInputStream().read();
            
            // Si no falta por leer información enviada desde el navegador, método read() devolverá -1.
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
	
	/** Método que escribe bytes desde un socket de entrada hacia un socket de salida.
	 * Es usado para enviar las respuestas del servidor al cliente.
	 * Los pasos que sigue este método son los siguientes:
	 * 1º - Obtenemos los streams ("corrientes de datos") de los sockets, necesarios para
	 * pasarse la información entre los mismos.
	 * 2º - Mientras hayan bytes pendientes de pasar en el socket de entrada, escribimos los bytes desde este
	 * hasta el de salida.
	 * @param socketEntrada es el canal que contiene los datos a pasar
	 * @param socketSalida es el canal de comunicación que recibe datos
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
                	// Almacenamos lo leído en el buffer (temporalmente)
                    bytes_leidos = streamEntrada.read(buffer);
                    
                    if (bytes_leidos > 0) { // Mientras haya contenido que escribir
                        streamSalida.write(buffer, 0, bytes_leidos); // Pasamos el contenido del buffer al stream
                        if (streamEntrada.available() < 1) {
                            streamSalida.flush();
                        }
                    }
                }
            } finally { // Cerramos los canales de comunicación (socket cliente y servidor)
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
