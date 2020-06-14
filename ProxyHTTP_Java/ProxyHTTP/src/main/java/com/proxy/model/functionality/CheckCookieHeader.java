package com.proxy.model.functionality;

import java.util.List;

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
	boolean isAnOptionActive() {
		return UserConfiguration.getInstance().getConfiguration().getCheckIfCookieHeader().equalsIgnoreCase("true");
	}

	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		if (UserConfiguration.getInstance().getConfiguration() != null && isAnOptionActive()) {
			String cookie = "Cookie";
			String set_cookie = "Set-Cookie";
			List<Header> headers = operation.getHeaders();
			for (Header header : headers) {
				if (header.getKey().equalsIgnoreCase(cookie) || header.getKey().equalsIgnoreCase(set_cookie)) {
					operation.getHeaders().remove(header);
				}
			}
		}

		return getFunctionality().modify(operation);
	}
}
