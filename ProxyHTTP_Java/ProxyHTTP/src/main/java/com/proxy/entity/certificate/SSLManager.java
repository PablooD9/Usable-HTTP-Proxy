package com.proxy.entity.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.proxy.entity.SingleX509KeyManager;

/**
 * @author Pablo
 *
 *
 *	NOTES:
 *	
	 CN: CommonName
	 OU: OrganizationalUnit
	 O: Organization
	 L: Locality
	 S: State Or Province Name
	 C: CountryName
 *
 */
public class SSLManager {
	
	/*
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		try {
			System.err.println( "Generating CA and End-Entity certs... ");
			
			generateCACertificate();
			
			System.err.println( "CA and End-Entity certs generated!");
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		}
	}
	*/
	private X509Certificate[] certsChain;
	private X509Certificate CACertificate;
	private PrivateKey caPrivateKey;

	private Date startDate, endDate;
	
	private final String CAKeyStore = "ProxyCA.p12";
	private final String ksPassword = "PassWorD!";
	private final String ksType = "PKCS12";
//	private final String ksType = "JKS";
	
	private final String CAAlias = "proxyca";
	
	public SSLManager() {
		setDates();
		setSystemProperties();
		
		Security.addProvider(new BouncyCastleProvider()); 
		generateCACertificate(false);
	}
	
	public void createCertificates() {
		generateCACertificate(false);
		generateEndEntityCert("localhost");
	}
	
	private void setDates() {
		// ====== Validity Date =====
		// Actual Date
		long now = System.currentTimeMillis();
	    startDate = new Date(now);
	    
	    // Adding 10 years to startDate
	    Calendar c = Calendar.getInstance();
	    c.setTime( startDate );
	    c.add(Calendar.YEAR, 10);
	    endDate = c.getTime();
	}
	
	// With Bouncy Castle
	public void generateCACertificate(boolean fileExistsButNotImportedIntoKS)
	{
		File caFile = new File("ProxyCACert.pem");
		if (!caFile.exists() || fileExistsButNotImportedIntoKS)
		{
			try {
				System.err.println("===> Creating CA Certificate! <===");
				// Create self signed Root CA certificate  
			    KeyPair CAkp = generateKeyPair();  
			    
			    X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(  
			         new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=ProxyTFGCA"), // issuer authority  
			         BigInteger.valueOf(new Random().nextInt()), //serial number of certificate  
			         startDate, // start of validity  
			         endDate, //end of certificate validity  
			         new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=ProxyTFGCA"), // subject name of certificate  
			         CAkp.getPublic()); // public key of certificate  
			    
			    // key usage restrictions  
			    builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));  
			    builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(true));  
			    X509Certificate CACert = new JcaX509CertificateConverter().getCertificate(builder  
			    		.build(new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC")
			    		.build(CAkp.getPrivate()))); // private key of signing authority , here it is self signed
			    
			    CACertificate = CACert;
			    certsChain = new X509Certificate[1]; // this array will store the CACertificate
			    certsChain[0] = CACertificate;
			    caPrivateKey = CAkp.getPrivate();
			    
			    saveCACertToFile();
			    System.err.println("<=== CA Certificate created! ===>");
			    
			    saveCertToKeyStore( CAAlias, CAkp.getPrivate(), certsChain);
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperatorCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else
		{
			loadCAFromKeyStore(new File(CAKeyStore));
		}
	}
	
	private void loadCAFromKeyStore(File CAKeyStoreFile) {
		
		InputStream in = null; 
		try {
//			in = new FileInputStream(CAKeyStoreFile);
			in = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
			
			KeyStore keyStore = KeyStore.getInstance( ksType );
//			keyStore.load(in, ksPassword.toCharArray());
			keyStore.load(in, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
			if (!keyStore.containsAlias(CAAlias))
			{
				generateCACertificate(true);
				return ;
			}
			
//			caPrivateKey = (PrivateKey) keyStore.getKey(CAAlias, ksPassword.toCharArray());
			caPrivateKey = (PrivateKey) keyStore.getKey(CAAlias, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
			Certificate[] chain = keyStore.getCertificateChain(CAAlias);
				
			certsChain = new X509Certificate[chain.length];
			System.arraycopy(chain, 0, certsChain, 0, chain.length);
			
			CACertificate = certsChain[0];
		    
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void generateEndEntityCert(String hostname){
		
		if(!certAlreadyGeneratedFor(hostname)) {
			//create end user cert signed
		    KeyPair endEntityCertKeyPair = generateKeyPair();  
		    
		    X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(  
		         CACertificate, // issuer authority (CACert)
		         BigInteger.valueOf(new Random().nextInt()), 
		         startDate,  
		         endDate,  
		         new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=" + hostname), // subject
		         endEntityCertKeyPair.getPublic());  
		    try {
				builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
				
				builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));  
			    X509Certificate endUserCert;
				endUserCert = new JcaX509CertificateConverter().getCertificate(
							builder.build(
									new JcaContentSignerBuilder("SHA256withRSA")
									.setProvider("BC")
							.build(caPrivateKey))); // private key of signing authority
				
				X509Certificate[] certsChain = getCertChain( endUserCert );
			    saveCertToKeyStore( hostname, endEntityCertKeyPair.getPrivate(), certsChain );
		    } catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			} catch (OperatorCreationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			} catch (CertIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		    
		}
			
	}
	
	private X509Certificate[] getCertChain(X509Certificate userCert) {
		X509Certificate[] chain = new X509Certificate[certsChain.length + 1];
		System.arraycopy(certsChain, 0, chain, 1, certsChain.length);
		chain[0] = userCert;
		return chain;
	}
	
	private boolean certAlreadyGeneratedFor(String hostname) {
//		File file = new File(hostname + ".p12");
//		return (file.exists()) ? true : false;
		
		File keystoreFile = new File(System.getProperty("javax.net.ssl.keyStore"));
		FileInputStream is=null;
		try {
			is = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
		
			KeyStore keystore = KeyStore.getInstance( ksType );
			keystore.load(is, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
			if (keystore.containsAlias( hostname ))
			{
				return true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static KeyPair generateKeyPair() {  
	    KeyPairGenerator kpGen=null;
		try {
			kpGen = KeyPairGenerator.getInstance("RSA", "BC");
			kpGen.initialize(1024); 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return kpGen.generateKeyPair(); 
	}  
	
	private void saveCACertToFile() {
		FileWriter fw = null;
		try {
	    	fw = new FileWriter("ProxyCACert.pem"); 
			fw.write(getCACert());
			fw.flush();  
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
	}
	
	private void saveCertToKeyStore(String hostnameAsAlias, PrivateKey certPrivKey, X509Certificate[] certsChain) {
		/*
		OutputStream os;	
		KeyStore ks;
			try {
				os = new FileOutputStream(hostnameAsAlias + ".p12");
				ks = KeyStore.getInstance( ksType );
				ks.load( null, ksPassword.toCharArray() );
				
//				if (certsChain.length < 2) // CA certificate if .length < 2
					addEntryToKS(hostnameAsAlias, certPrivKey, certsChain, ks);
//				else
//					addEntryToKS(hostnameAsAlias, certsChain[1], ks);
				initializeKSfromKS( ks, os );
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/
		
		
		File keystoreFile = new File(System.getProperty("javax.net.ssl.keyStore"));
		FileInputStream is=null;
		try {
			// Load the Java Default KeyStore content into an auxiliar "keystore"
			is = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
			KeyStore keystore = KeyStore.getInstance( ksType );
			keystore.load(is, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
			if (keystore.containsAlias(hostnameAsAlias))
			{
				System.out.println("Alias is in the KS! ");
				keystore.deleteEntry(hostnameAsAlias);
			}
//			else
//			{
			// Add the certificate to the auxiliar keystore
			keystore.setKeyEntry(hostnameAsAlias, certPrivKey, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray(), certsChain);
			
			// Save the new keystore content from the auxiliar to the "real" Java Default KeyStore
			FileOutputStream out = new FileOutputStream(keystoreFile);
			keystore.store(out, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			out.close();
//			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public SSLContext generateContext(String host) 
	{
		KeyManagerFactory kmf;
		try {
			kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			
//			KeyStore ks = getKeyStore(host);
			KeyStore ks = getKeyStore();
//			kmf.init(ks, ksPassword.toCharArray());
			kmf.init(ks, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "X509" );
			tmf.init(ks);
			
//			PrivateKey pk = (PrivateKey) ks.getKey(host, ksPassword.toCharArray());
			PrivateKey pk = (PrivateKey) ks.getKey(host, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			Certificate[] chain = ks.getCertificateChain(host);
			X509Certificate[] certChain = new X509Certificate[chain.length];
			System.arraycopy(chain, 0, certChain, 0, chain.length);
			
			X509KeyManager km = new SingleX509KeyManager(host,
					pk, certChain);

			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(new KeyManager[] { km }, tmf.getTrustManagers(), new SecureRandom());
//			sslContext.init(new KeyManager[] { km }, new TrustManager[] { new DefaultTrustManager() }, null);
			
			return sslContext;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private KeyStore getKeyStore(String hostname) {
		File file = new File(hostname + ".p12");
		InputStream in = null;
		KeyStore ks = null;
		try {
			in = new FileInputStream(file);
			ks = KeyStore.getInstance( ksType );
			ks.load(in, ksPassword.toCharArray());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ks;
	}
	
	private KeyStore getKeyStore() {
		File file = new File(System.getProperty("javax.net.ssl.keyStore"));
		InputStream in = null;
		KeyStore ks = null;
		try {
			in = new FileInputStream(file);
			ks = KeyStore.getInstance( ksType );
			ks.load(in, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ks;
	}
	
	private String getCACert() throws CertificateEncodingException {
		Base64.Encoder encoder = Base64.getEncoder();
		return "-----BEGIN CERTIFICATE-----\n"
				+ encoder.encodeToString(certsChain[0].getEncoded())
				+ "\n-----END CERTIFICATE-----\n";
	}
	
	
	private void setSystemProperties() {
//		System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,connection,content-length,expect,upgrade");
		
		// TODO CAMBIAR!!!! Crear nuestra propia keystore, ya que cacerts puede corromperse.
		System.setProperty("javax.net.ssl.keyStore", System.getenv("JAVA_HOME") + "/lib/security/cacerts");
		System.setProperty("javax.net.ssl.keyStorePassword", "changeit");

		System.setProperty( "sun.security.ssl.allowUnsafeRenegotiation", "true" );
		
//		System.setProperty("javax.net.debug", "all");
		
	}
	

}


class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

