package com.proxy.model.hosttype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import com.proxy.parser.HostParser;

/** Clase abstracta que define un Host dentro de la aplicación.
 * @author Pablo
 *
 */
public abstract class Host implements Cloneable {
	private Integer _id;
	private String hostName;
	private List<Host> hostList = new ArrayList<>();
	private final static Logger LOG = Logger.getLogger(Host.class);

	public Host() {}
	
	public Host(Integer _id, String hostName) {
		this._id = _id;
		this.hostName = hostName;
	}

	public Integer get_id() { return _id; }
	public void set_id(Integer _id) {
		this._id = _id;
	}

	public String getHostName() { return hostName; }
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public List<Host> getHostList() { return hostList; }
	public void setHostList(List<Host> hostList) {
		this.hostList = hostList;
	}
	
	/** Obtiene la lista de hosts a partir de una URL.
	 * Los hosts obtenidos son parseados e insertados en una lista.
	 * @return Lista de Host.
	 */
	public List<Host> loadHostsList(){
		HostParser parser = new HostParser();
		URL url;
		URLConnection conn;
		BufferedReader reader = null;
		try {
			url = new URL( getURLOfHostList() );
			conn = url.openConnection();
			
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			int counter = 1;
			while ( (line = reader.readLine() ) != null) {
				if ( (line = parser.parse(line)) != null) {
					Host hostCloned = clone(counter, line);
					hostList.add( hostCloned );
					counter++;
				}
			}
		} catch (MalformedURLException e) {
			LOG.log(Level.ERROR, "La URL está mal formada." + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida." + e.getMessage());
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					LOG.log(Level.ERROR, "Error de entrada/salida." + e.getMessage());
				}
		}
		return hostList;
	}
	
	private Host clone(Integer _id, String host) {
		Object clone = null;
		this._id = _id;
		this.hostName = host;
		
		try {
			clone = super.clone();
		} catch (CloneNotSupportedException e) {
			LOG.log(Level.ERROR, "La clase no permite ser clonada. " + e.getMessage());
		}
		
		return (Host) clone;
	}
	
    @Override
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Host)) {
            return false;
        }
        Host otherMember = (Host)anObject;
        return otherMember._id.equals(this._id);
    }
	
	/** Método a implementar por parte de las subclases, que permite establecer la
	 * URL a partir de la cual se obtienen los hosts de un tipo concreto.
	 * @return una URL.
	 */
	public abstract String getURLOfHostList();
}
