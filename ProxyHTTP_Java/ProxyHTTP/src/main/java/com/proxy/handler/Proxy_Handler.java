package com.proxy.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
    		
    		prepararRespuesta();
			
    	} catch (NumberFormatException nfe) {
			System.err.println("Error: Server port is not an integer.");
			// TODO Almacenar error en log
			nfe.printStackTrace();
        	escribeEncabezadoRespuesta( 505 );
            return ;
        } catch (IOException ioe) {
    		System.out.print("Error [3]! :>>> ");
    		// TODO Almacenar error en log
    		ioe.printStackTrace();
    	} catch (ArrayIndexOutOfBoundsException aioobe){
    		System.out.print("Error: Impossible to get URL or server port (URL without colon?) :>>> ");
    		// TODO Almacenar error en log
    		aioobe.printStackTrace();
    	} finally {
            try {
            	if (!socketCliente.isClosed())
            		socketCliente.close();
            } catch (IOException e) {
            	System.out.print("Error [4]! :>>> ");
            	// TODO Almacenar error en log
                e.printStackTrace();  
            }
        }
	}

	private void prepararRespuesta() throws IOException {
		if (versionHTTP != null && URL != null) {
			try{
				socketServidor = new Socket( URL, puertoServidor );
				
			} catch (NumberFormatException nfe) {
				System.err.println("Error: Impossible to connect (Unknown Server Port) :>>> " + puertoServidor);
				// TODO Almacenar error en log
		    	escribeEncabezadoRespuesta( 505 );
		    	return ;
			} catch (UnknownHostException uhe) {
				System.err.println("Error: Impossible to connect (Unknown Host) :>>> " + URL);
				// TODO Almacenar error en log
				uhe.printStackTrace();
				escribeEncabezadoRespuesta( 505 );
		    	return ;
		    }
			
			escribeEncabezadoRespuesta( 200 );
			enviarRespuesta();
			
			if (!socketServidor.isClosed())
				socketServidor.close();
		}
	}
	
	/** M�todo que recoge una petici�n desde el cliente, leyendo la primera l�nea y sacando de la misma la URL del servidor,
	 * el puerto remoto de dicha URL y la versi�n del protocolo HTTP.
	 * El formato de lo devuelto es, entre otro contenido:
	 * 
	 * CONNECT ab.blogs.es:443 HTTP/1.1
	 * 
	 * @return la URL y el puerto remoto. En el ejemplo anterior, "ab.blogs.es:443"
	 */
	private String recogePeticion() {
		String URLYPuerto = null;
		try {
			// Obtenemos la primera l�nea de la petici�n (obtenemos la URL, versi�n HTTP y puerto remoto)		
			String lineaPeticion = leerPeticion();
		
			versionHTTP = obtenerVersionHTTP( lineaPeticion );
			URLYPuerto = obtenerURL( lineaPeticion );
			
			if (URLYPuerto == null) {
				throw new IllegalStateException("Error en la obtenci�n de la URL y el puerto remoto");
			}
				
			// Una vez recogida la URL y el puerto, continuamos procesando las distintas 
			// cabeceras de la petici�n hasta que no quede ninguna.
			String aux = leerPeticion();
			while (!aux.equals(""))
				aux = leerPeticion();
			
//			System.out.println( lineaPeticion );
			
		} catch (IOException e) {
			System.err.println("Error X :>>> ");
			// TODO Almacenar error en log
			e.printStackTrace();
		}
		
		return URLYPuerto;
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
     * @return
     * @throws IOException en el caso en el que no se pueda leer del b�fer
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
		
		// System.out.println( contenidoPeticionCliente.toString() );
		
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
            if (read != -1) {
                socketServidor.getOutputStream().write(read);
                enviarDatosServidorACliente(socketCliente, socketServidor);
            } else {
               if (!socketServidor.isOutputShutdown()) {
                    socketServidor.shutdownOutput();
                }
                if (!socketCliente.isInputShutdown()) {
                    socketCliente.shutdownInput();
                }
            }
        } catch (IOException e) {
        	System.err.println("Error: Couldn't read from the buffer :>>> ");
        	// TODO Almacenar error en log
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
	 * << En futuras versiones, este m�todo ser� sustitu�do por un Factory. >>
	 * Devuelve encabezados HTTP, dependiendo del c�digo HTTP correspondiente.
	 */
	private void escribeEncabezadoRespuesta(int codigoError) {
		OutputStreamWriter streamWriterSalida;
		try {
			streamWriterSalida = new OutputStreamWriter(socketCliente.getOutputStream(), ISO);
			
			switch (codigoError) {
				case 200:
					streamWriterSalida.write(versionHTTP + " 200\r\n");
//        			streamWriterSalida.write("Proxy-agent: Simple/0.1\r\n");
        			streamWriterSalida.write("\r\n");
        			streamWriterSalida.flush();
        			break;
				case 502:
					streamWriterSalida.write(versionHTTP + " 502\r\n");
//                  streamWriterSalida.write("Proxy-agent: Simple/0.1\r\n");
                    streamWriterSalida.write("\r\n");
                    streamWriterSalida.flush();
                    break;
			}
			
		} catch (IOException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		}
		
	}
	
	private String obtenerURL(String cadenaPeticion) {
		//return cadenaPeticion.contains("CONNECT ") ? cadenaPeticion.split(" ")[1] : null;
		
		if (cadenaPeticion.contains("CONNECT ") ||
				cadenaPeticion.contains("POST ") ||
					cadenaPeticion.contains("GET "))
		{
			return cadenaPeticion.split(" ")[1].replaceAll("http[s]*://", "");
		}
		
		return null;
	}
	
	private String obtenerVersionHTTP(String cadenaPeticion) {
//		return cadenaPeticion.contains("CONNECT ") ? cadenaPeticion.split(" ")[2] : null;
	
		if (cadenaPeticion.contains("CONNECT ") ||
				cadenaPeticion.contains("POST ") ||
					cadenaPeticion.contains("GET "))
		{
			return cadenaPeticion.split(" ")[2];
		}
		
		return null;
	}
	
}
