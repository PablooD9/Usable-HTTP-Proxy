package com.proxy.model.functionality;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;

/** Clase que modifica la cabecera User-Agent de una operación HTTP. La cabecera User-Agent establecida
 * será la correspondiente a la elegida por el usuario.
 * @author Pablo
 *
 */
public class CheckUserAgentHeader extends CheckProxyFunctionality {

	public CheckUserAgentHeader(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	public boolean isAnOptionActive() {
		return true;
	}
	
	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (UserConfiguration.getInstance().getConfiguration() != null) {
			String userAgent = UserConfiguration.getInstance().getConfiguration().getUserAgent();
			if (userAgent != null) {
				operation.setHeader("User-Agent", userAgent);
			}
		}
		return getFunctionality().modify(operation);
	}
}
