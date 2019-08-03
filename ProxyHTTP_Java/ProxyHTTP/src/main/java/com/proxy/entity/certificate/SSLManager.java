package com.proxy.entity.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
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
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
	private KeyPair CAKeyPair;
	private Date startDate, endDate;
	private PrivateKey caPrivateKey;
	
	private File CAKeyStoreFile;
	
	private final String CAKeyStore = "ProxyCA.p12";
	private final String ksPassword = "PassWorD!";
	private final String ksType = "PKCS12";
	private final String CAAlias = "ProxyCA";
	
	public SSLManager() {
		setDates();
		CAKeyStoreFile = new File( CAKeyStore );
		Security.addProvider(new BouncyCastleProvider()); 
		
		try {
			generateCACertificate();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	

	public void generateCAandEndUserCertificates(String hostname) throws CertificateException, OperatorCreationException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException, UnrecoverableEntryException {
		generateCACertificate();
		generateEndEntityCert(hostname);
	}
	
	// With Bouncy Castle
	public void generateCACertificate() throws NoSuchAlgorithmException, NoSuchProviderException, CertificateException, OperatorCreationException, IOException, KeyStoreException, UnrecoverableEntryException
	{
		File caFile = new File("ProxyCACert.pem");
		if (!caFile.exists())
		{
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
		    CAKeyPair = CAkp;
		    certsChain = new X509Certificate[1]; // this array will store the CACertificate
		    certsChain[0] = CACertificate;
		    
		    saveCACertToFile();
		    System.err.println("<=== CA Certificate created! ===>");
		    
		    saveCertToKeyStore( CAAlias, CAKeyPair.getPrivate(), certsChain);
		}
		else
		{
			loadCAFromKeyStore(new File(CAKeyStore));
		}
	}
	
	private void loadCAFromKeyStore(File CAKeyStoreFile) {
		if (!CAKeyStoreFile.exists())
			return ;
		
		InputStream in = null; 
		try {
			in = new FileInputStream(CAKeyStoreFile);
			
			KeyStore keyStore = KeyStore.getInstance( ksType );
			keyStore.load(in, ksPassword.toCharArray());
			caPrivateKey = (PrivateKey) keyStore.getKey(CAAlias, ksPassword.toCharArray());
			Certificate[] chain = keyStore.getCertificateChain(CAAlias);
				
			certsChain = new X509Certificate[chain.length];
			System.arraycopy(chain, 0, certsChain, 0, chain.length);
			
			CACertificate = certsChain[0];
			
			// Create self signed Root CA certificate  
		    KeyPair CAkp = generateKeyPair();  
			CAKeyPair = CAkp;
		    
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
							.build(CAKeyPair.getPrivate()))); // private key of signing authority
				
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
		File file = new File(hostname + ".p12");
		return (file.exists()) ? true : false;
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
		OutputStream os;	
		KeyStore ks;
			try {
				os = new FileOutputStream(hostnameAsAlias + ".p12");
				ks = KeyStore.getInstance( ksType );
				ks.load( null, ksPassword.toCharArray() );
				addEntryToKS(hostnameAsAlias, certPrivKey, certsChain, ks);
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
		
	}

	
	private void initializeKSfromKS(KeyStore ks, OutputStream out) {
		try {
			ks.store(out, ksPassword.toCharArray());
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
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	
	private void addEntryToKS(String hostnameAsAlias, PrivateKey certPrivKey, X509Certificate[] certsChain, KeyStore ks)
			throws KeyStoreException {
		ks.setKeyEntry(hostnameAsAlias, certPrivKey, ksPassword.toCharArray(), certsChain);
	}

	public SSLContext generateContext(String host) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, KeyStoreException, 
																												IOException, UnrecoverableKeyException, KeyManagementException 
	{
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
		
		KeyStore ks = getKeyStore(host);
		kmf.init(ks, ksPassword.toCharArray());
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
		tmf.init(ks);
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		
		return sslContext;
	}
	
	private KeyStore getKeyStore(String hostname) {
		File file = new File(hostname + ".p12");
		InputStream in;
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
		}
		return ks;
	}
	
	private String getCACert() throws CertificateEncodingException {
		try {
			// TODO intentar cambiar esto, para ver si se puede prescindir de la mierda esa de Base64.
			return "-----BEGIN CERTIFICATE-----\n"
					+ Base64(certsChain[0].getEncoded(), // certsChain[0] => CA cert
							Base64.)
					+ "\n-----END CERTIFICATE-----\n";
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
}
