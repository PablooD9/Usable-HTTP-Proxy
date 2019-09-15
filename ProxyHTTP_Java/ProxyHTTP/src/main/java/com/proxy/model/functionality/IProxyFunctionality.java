package com.proxy.model.functionality;

import com.proxy.interceptor.request.IHttpRequest;

public interface IProxyFunctionality {
	String modifyRequest(IHttpRequest request);
}
