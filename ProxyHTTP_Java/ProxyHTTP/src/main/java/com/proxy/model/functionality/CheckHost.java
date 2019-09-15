package com.proxy.model.functionality;

import java.util.List;

import com.proxy.interceptor.request.IHttpRequest;
import com.proxy.model.hosttype.Host;
import com.proxy.services.HostService;

public abstract class CheckHost extends ProxyDecorator {

	private List<Host> hostsList;
	private HostService hostService = new HostService();
	
	public CheckHost(IProxyFunctionality functionality) {
		super(functionality);
	}
	
	void setHostsList(List<Host> hostsList) {
		this.hostsList = hostsList;
	}

	public HostService getHostService() {
		return hostService;
	}

	@Override
	public String modifyRequest(IHttpRequest request) {
		// TODO Auto-generated method stub
		if (hostIsInHostsDangerousList( request.getHost() ))
			return null;
		
		return getFunctionality().modifyRequest( request );
	}

	private boolean hostIsInHostsDangerousList(String hostToFind) {
		loadHostsList();
		
		Host hostFound = hostsList.stream().filter(host -> host.getHostName().equalsIgnoreCase(hostToFind))
							.findFirst()
							.orElse(null);
		
		if (hostFound == null) {
			System.out.println("Host no está!!");
			return false;
		}
		
		return true;
	}
	
	abstract void loadHostsList();
}
