package com.proxy.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.proxy.model.functionality.CheckMaliciousHost;
import com.proxy.model.functionality.CheckPornographyHost;
import com.proxy.model.functionality.CheckProxyFunctionality;
import com.proxy.model.functionality.CheckSpanishMaliciousHost;
import com.proxy.model.functionality.CheckTrackerHost;
import com.proxy.model.hosttype.AbstractHostComposite;
import com.proxy.model.hosttype.CreateHost;
import com.proxy.model.hosttype.CreateHostFactory;
import com.proxy.model.hosttype.Host;
import com.proxy.model.hosttype.HostComposite;
import com.proxy.model.hosttype.HostType;

/** Clase que maneja los ficheros que contienen los distintos tipos hosts peligrosos.
 * @author Pablo
 *
 */
@Service
public class HostService {

	private final static String MALICIOUS_HOSTS = "static/otherFiles/MaliciousHosts.txt";
	private final static String PORNOGRAPHY_HOSTS = "static/otherFiles/PornographyHosts.txt";
	private final static String SPANISH_MALICIOUS_HOSTS = "static/otherFiles/SpanishMaliciousHosts.txt";
	private final static String TRACKERS_HOSTS = "static/otherFiles/TrackersHosts.txt";

	private final static Logger LOG = Logger.getLogger(HostService.class);
	
	/** 
	 * Actualiza las listas de hosts de la base de datos, y crea/actualiza los correspondientes ficheros de hosts.
	 */
	public void updateHostsList() {
		
		AbstractHostComposite hostComposite = new HostComposite();
		
		hostComposite.addHostType( HostType.Malicious_Hosts );
		hostComposite.addHostType( HostType.Trackers_Hosts );
		hostComposite.addHostType( HostType.Spanish_Malicious_Hosts );
		hostComposite.addHostType( HostType.Pornography_Hosts );
		
//		hostComposite.updateMongoHostsList();
		createHostFiles();
	}
	
	/** Carga el contenido de un fichero dentro de la aplicación.
	 * @param file Fichero del cual se leerá y cargará el contenido.
	 * @param hostType Tipo de los hosts que se cargarán.
	 */
	private List<Host> loadFromFile(String filepath, HostType hostType) {
		InputStream is = loadLocalFile(filepath);
		Reader reader = new InputStreamReader(is);
		BufferedReader buffReader = null;
		List<Host> hostsList = new ArrayList<>();
		try {
			buffReader = new BufferedReader(reader);
			String line = "";
			while ((line = buffReader.readLine()) != null) {
				CreateHostFactory chf = new CreateHost();
				Host host = chf.createHost(hostType);
				
				String[] attributes = line.trim().split("[ ]+");
				if (attributes.length == 2) {
					host.set_id( Integer.parseInt(attributes[0] ));
					host.setHostName(attributes[1]);
					hostsList.add(host);
				}
			}
		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Fichero de hosts no encontrado. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error al leer del fichero de hosts. " + e.getMessage());
		} finally {
			try {
				is.close();
				reader.close();
				buffReader.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de Entrada/Salida. " + e.getMessage());
			}
		}
		return hostsList;
	}
	
	public List<Host> getHostsFromActiveOptions(){
		List<Host> hosts = new ArrayList<Host>();
		
		CheckProxyFunctionality functionality = new CheckMaliciousHost();
		if (functionality.isAnOptionActive()) {
			hosts.addAll(loadFromFile(MALICIOUS_HOSTS, HostType.Malicious_Hosts));
		}
		
		functionality = new CheckSpanishMaliciousHost();
		if (functionality.isAnOptionActive()) {
			hosts.addAll(loadFromFile(SPANISH_MALICIOUS_HOSTS, HostType.Spanish_Malicious_Hosts));
		}
		
		functionality = new CheckTrackerHost();
		if (functionality.isAnOptionActive()) {
			hosts.addAll(loadFromFile(TRACKERS_HOSTS, HostType.Trackers_Hosts));
		}
		
		functionality = new CheckPornographyHost();
		if (functionality.isAnOptionActive()) {
			hosts.addAll(loadFromFile(PORNOGRAPHY_HOSTS, HostType.Pornography_Hosts));
		}
		
		return hosts;
	}

	private static InputStream loadLocalFile(String filename) {
		try {
			Resource resource = new ClassPathResource(filename);
			InputStream is = resource.getInputStream();
			return is;
		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Fichero no encontrado. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error al leer el contenido del fichero. " + e.getMessage());
		}
		return null;
	}
}
