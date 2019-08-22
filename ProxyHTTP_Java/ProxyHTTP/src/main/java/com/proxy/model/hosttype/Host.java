package com.proxy.model.hosttype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.proxy.parser.HostParser;

public abstract class Host implements Cloneable{
	private Integer _id;
	private String hostName;
	private List<Host> hostList = new ArrayList<>();

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

	
	/** Obtiene la lista de hosts maliciosos desde la URL:
	 * 		"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn-social/hosts"
	 * Los hosts obtenidos de la página anterior son parseados e insertados en una lista.
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
					hostList.add( clone(counter, line) );
					counter++;
				}
			}
		} catch (MalformedURLException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Almacenar error en log
					e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (Host) clone;
	}
	
	public abstract String getURLOfHostList();
}
