package com.proxy.model.functionality;

import java.util.Iterator;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.interceptor.httpOperation.request.Header;
import com.proxy.model.UserConfiguration;

/**
 * Clase que nos permite modificar una respuesta HTTP para que no incluya
 * Cookies que, en algunos casos, son usadas con fines malintencionados.
 * 
 * @author Pablo
 *
 */
public class CheckCookieHeader extends CheckProxyFunctionality {

	public CheckCookieHeader(IProxyFunctionality functionality) {
		super(functionality);
	}

	@Override
	public boolean isAnOptionActive() {
		if (UserConfiguration.getInstance().getConfiguration().getCheckIfCookieHeader() != null) {
			return UserConfiguration.getInstance().getConfiguration().getCheckIfCookieHeader().equalsIgnoreCase("true");
		}
		else {
			return false;
		}
	}

	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive()) {
			String cookie = "Cookie";
			String set_cookie = "Set-Cookie";
			Iterator<Header> headers = operation.getHeaders().iterator();
			while (headers.hasNext()) {
				Header header = headers.next();
				if (header.getKey().equalsIgnoreCase(cookie) || header.getKey().equalsIgnoreCase(set_cookie)) {
					headers.remove();
				}
			}
		}

		return getFunctionality().modify(operation);
	}
}
