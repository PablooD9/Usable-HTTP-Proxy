package com.proxy.model.functionality;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.model.UserConfiguration;

public class ModifyUserAgent extends ProxyDecorator {
	
	public ModifyUserAgent(IProxyFunctionality functionality)
	{
		super( functionality );
	}
	
	@Override
	public IHttpOperation modify(IHttpOperation operation) {

		if (UserConfiguration.getInstance().getConfiguration() != null)
		{
			String userAgent = UserConfiguration.getInstance().getConfiguration().getOp1();
			if (userAgent != null) {
				operation.setHeader("User-Agent", userAgent);
			}
		}
		return getFunctionality().modify( operation );
	}
}
