package com.proxy.model.functionality;

import java.util.List;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;

public class CheckSecurityHeaders extends CheckHost {

	public CheckSecurityHeaders(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	boolean isAnOptionActive() {
		String headers = UserConfiguration.getInstance().getConfiguration().getOp6();
		return (headers != null && headers.length() > 1);
	}
	
	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive()) {
			if (checkIfThereIsSecurityException( operation ))
				return getFunctionality().modify( operation );
			String preHeaders = UserConfiguration.getInstance().getConfiguration().getOp6();
			String[] headers;
			if (preHeaders.contains(","))
				headers = preHeaders.split(",");
			else
				headers = new String[] { preHeaders };
			for (int i=0; i< headers.length; i++) {
				if (operation.getHeader( headers[i] ) == null) {
					return null; // Host does not implement the security header in their responses.
				}
				else
					System.out.println("El host " + operation.getHost() + " SÍI implementa la cabecera " + headers[i] + " :))");
			}
		}
		
		return getFunctionality().modify( operation );
	}
	
	private boolean checkIfThereIsSecurityException(IHttpOperation operation) {
		String host = operation.getHost();
		List<String> hostExceptions = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		for (String exception : hostExceptions)
			if (exception.equalsIgnoreCase(host))
				return true;
		
		return false;
	}

}
