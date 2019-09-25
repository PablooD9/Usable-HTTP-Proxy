package com.proxy.model.functionality;

import com.proxy.interceptor.IHttpOperation;
import com.proxy.interceptor.httpOperation.request.IHttpRequest;

public interface IProxyFunctionality {
	IHttpOperation modify(IHttpOperation operation);
}
