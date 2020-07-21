package com.proxy.interceptor.certificate;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/** 
 * @author Pablo
 *
 */
public class TrustManagerHandler {
	
	private X509TrustManager trustManager;
	private KeyStore keyStore;
	
	public TrustManagerHandler(KeyStore ks) {
		this.keyStore = ks;
	}
	
	public X509TrustManager getTrustManager() {
		return trustManager;
	}

	public void initTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "X509" );
		tmf.init((KeyStore) keyStore);
		TrustManager trustManagers[] = tmf.getTrustManagers();
        for (int i = 0; i < trustManagers.length; i++) {
            if (trustManagers[i] instanceof X509TrustManager) {
                this.trustManager = (X509TrustManager) trustManagers[i];
                break;
            }
        }
        
        if (trustManager == null) {
        	trustManager = new X509TrustManager() {
				
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
				
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			};
        }
	}
	
}
