package com.proxy.model.functionality;

import java.util.List;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;
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
	public IHttpOperation modify(IHttpOperation operation) {
		// TODO Auto-generated method stub
		if (hostIsInHostsDangerousList( operation.getHost() ))
			return null;
		
		return getFunctionality().modify( operation );
	}

	private boolean hostIsInHostsDangerousList(String hostToFind) {
		
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive())
		{
			loadHostsList();
		}
		if (hostsList == null)
			return false;
		
		Host hostFound = hostsList.stream().filter(host -> host.getHostName().equalsIgnoreCase(hostToFind))
							.findFirst()
							.orElse(null);
		
		if (hostFound == null) { // Host not found (that is good)
			System.out.println("Host no está!!");
			return false;
		}
		else
			System.out.println("Ø PELIGRO Ø" + hostFound.getHostName());
		
		return true;
	}
	
	private void loadHostsList() {
		setHostsList( UserConfiguration.getInstance().getMaliciousHostsToScan() );
	}
	
	abstract boolean isAnOptionActive();
}
