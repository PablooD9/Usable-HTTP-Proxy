package main;

/** Clase encargada de almacenar la configuración del Proxy
 * @author Pablo
 *
 */
public class Proxy_Config
{
	private final int puertoLocal;
    
    public Proxy_Config( int puerto ) {
    	this.puertoLocal = puerto;
    }

	public int getPuertoLocal() {
		return puertoLocal;
	}
    
}
