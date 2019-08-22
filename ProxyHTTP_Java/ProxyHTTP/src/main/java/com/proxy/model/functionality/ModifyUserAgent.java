package com.proxy.model.functionality;

import com.proxy.entity.request.IHttpRequest;

public class ModifyUserAgent extends ProxyDecorator {

	private String userAgent;
	
	public ModifyUserAgent(String userAgent, IProxyFunctionality functionality)
	{
		super( functionality );
		this.userAgent = userAgent;
	}
	
	@Override
	public String modifyRequest(IHttpRequest request) {
		request.setHeader("User-Agent", userAgent);
		return getFunctionality().modifyRequest( request );
	}
}
