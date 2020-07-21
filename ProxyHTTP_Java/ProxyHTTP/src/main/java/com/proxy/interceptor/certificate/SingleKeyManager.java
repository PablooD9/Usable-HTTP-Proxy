package com.proxy.interceptor.certificate;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

public class SingleKeyManager implements X509KeyManager {

	private String alias;
	private PrivateKey pk;
	private X509Certificate[] certificateChain;

	public SingleKeyManager(String alias, PrivateKey pk,
			X509Certificate[] certs) {
		this.alias = alias;
		this.pk = pk;
		this.certificateChain = cloneCertChain(certs);
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return new String[] {alias};
	}
	
	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket) {
		return alias;
	}
	
	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers,
			Socket socket) {
		return alias;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return cloneCertChain(certificateChain);
	}
	
	@Override
	public PrivateKey getPrivateKey(String alias) {
		return pk;
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return new String[] { alias };
	}

	private X509Certificate[] cloneCertChain(X509Certificate[] certs) {
		if (certs == null)
			return null;
		X509Certificate[] copy = new X509Certificate[certs.length];
		System.arraycopy(certs, 0, copy, 0, certs.length);
		return copy;
	}
	
}
