package com.proxy.services;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.proxy.model.hosttype.AbstractHostComposite;
import com.proxy.model.hosttype.Host;
import com.proxy.model.hosttype.HostComposite;
import com.proxy.model.hosttype.HostType;

@Service
public class HostService {

//	@PostConstruct
	public void updateHostsList() {
		
		AbstractHostComposite hostComposite = new HostComposite();
		
		hostComposite.addHost( HostType.Malicious_Hosts );
		hostComposite.addHost( HostType.Trackers_Hosts );
		hostComposite.addHost( HostType.Spam_Hosts );
		hostComposite.addHost( HostType.Pornography_Hosts );
		
		hostComposite.updateHostsList();
	}
	
	public List<Host> getHostsFromType(HostType hostType){
		AbstractHostComposite hostComposite = new HostComposite();

		return hostComposite.obtainHostsList( hostType );
	}

}
