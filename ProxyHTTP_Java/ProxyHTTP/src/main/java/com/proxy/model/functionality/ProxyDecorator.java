package com.proxy.model.functionality;

public abstract class ProxyDecorator implements IProxyFunctionality {

	private IProxyFunctionality functionality;
	
	public ProxyDecorator(IProxyFunctionality functionality) {
		setFunctionality( functionality );
	}
	
	public IProxyFunctionality getFunctionality() { return functionality; }
	public void setFunctionality(IProxyFunctionality functionality) {
		this.functionality = functionality;
	}
	
}
