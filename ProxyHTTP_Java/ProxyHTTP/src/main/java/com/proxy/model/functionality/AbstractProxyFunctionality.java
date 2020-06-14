package com.proxy.model.functionality;

public abstract class AbstractProxyFunctionality implements IProxyFunctionality {

	private IProxyFunctionality functionality;

	public AbstractProxyFunctionality(IProxyFunctionality functionality) {
		setFunctionality(functionality);
	}

	public IProxyFunctionality getFunctionality() {
		return functionality;
	}

	public void setFunctionality(IProxyFunctionality functionality) {
		this.functionality = functionality;
	}

}
