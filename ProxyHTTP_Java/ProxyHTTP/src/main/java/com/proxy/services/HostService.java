package com.proxy.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.proxy.model.UserConfiguration;
import com.proxy.model.hosttype.AbstractHostComposite;
import com.proxy.model.hosttype.CreateHost;
import com.proxy.model.hosttype.CreateHostFactory;
import com.proxy.model.hosttype.Host;
import com.proxy.model.hosttype.HostComposite;
import com.proxy.model.hosttype.HostType;

@Service
public class HostService {

	private final static File MALICIOUS_HOSTS_FILE = new File( "src/main/resources/static/otherFiles/MaliciousHosts.txt" );
	
	private final static File PORNOGRAPHY_HOSTS_FILE = new File( "src/main/resources/static/otherFiles/PornographyHosts.txt" );
	
	private final static File SPAM_HOSTS_FILE = new File( "src/main/resources/static/otherFiles/SpamHosts.txt" );
	
	private final static File TRACKERS_HOSTS_FILE = new File( "src/main/resources/static/otherFiles/TrackersHosts.txt" );
	
	public void updateHostsList() {
		
		AbstractHostComposite hostComposite = new HostComposite();
		
		hostComposite.addHost( HostType.Malicious_Hosts );
		hostComposite.addHost( HostType.Trackers_Hosts );
		hostComposite.addHost( HostType.Spam_Hosts );
		hostComposite.addHost( HostType.Pornography_Hosts );
		
		hostComposite.updateHostsList();
		createHostFiles();
	}
	
	@PostConstruct
	public void loadFiles() {
		if ( !MALICIOUS_HOSTS_FILE.exists() ||
			 !PORNOGRAPHY_HOSTS_FILE.exists() ||
			 !SPAM_HOSTS_FILE.exists() ||
			 !TRACKERS_HOSTS_FILE.exists())
		{
			updateHostsList();
			createHostFiles();
		}
		else { // When the file exists, we can load the content of them
			loadFile(MALICIOUS_HOSTS_FILE, HostType.Malicious_Hosts);
			loadFile(PORNOGRAPHY_HOSTS_FILE, HostType.Pornography_Hosts);
			loadFile(SPAM_HOSTS_FILE, HostType.Spam_Hosts);
			loadFile(TRACKERS_HOSTS_FILE, HostType.Trackers_Hosts);
		}
	}
	
	private void loadFile(File file, HostType hostType) {
		BufferedReader reader = null;
		List<Host> hosts = new ArrayList<>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = reader.readLine()) != null) {
				CreateHostFactory chf = new CreateHost();
				Host host = chf.createHost(hostType);
				
				String[] attributes = line.trim().split("[ ]+");
				if (attributes.length == 2) {
					host.set_id( Integer.parseInt( attributes[0]  ));
					host.setHostName( attributes[1] );
					hosts.add( host );
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		UserConfiguration.getInstance().getMaliciousHostsToScan().addAll( hosts );
	}
	
	private void createHostFiles() {
		createFile(MALICIOUS_HOSTS_FILE, HostType.Malicious_Hosts);
		createFile(PORNOGRAPHY_HOSTS_FILE, HostType.Pornography_Hosts);
		createFile(SPAM_HOSTS_FILE, HostType.Spam_Hosts);
		createFile(TRACKERS_HOSTS_FILE, HostType.Trackers_Hosts);
	}
	
	private void createFile(File file, HostType hostType) {
		BufferedWriter writer=null;
		try {
			writer = new BufferedWriter(new FileWriter( file ));
			HostComposite hostComposite = new HostComposite();
			List<Host> hosts = hostComposite.loadHostList( hostType );
			int counter=0;
			for (Host host : hosts) {
				if (counter++ == hosts.size()-1) // last option
					writer.write(host.get_id() + " " + host.getHostName());
				else
					writer.write(host.get_id() + " " + host.getHostName() + "\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
