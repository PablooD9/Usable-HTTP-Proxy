package com.proxy.model.functionality;

import com.proxy.interceptor.IHttpOperation;

public class ProxyFunctionalityImpl implements IProxyFunctionality {

	public ProxyFunctionalityImpl() {

	}

	@Override
	public IHttpOperation modify(IHttpOperation operation) {
		return operation;
	}

}
