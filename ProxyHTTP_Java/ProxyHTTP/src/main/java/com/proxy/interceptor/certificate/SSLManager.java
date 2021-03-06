package com.proxy.interceptor.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.springframework.util.ResourceUtils;

/** Clase encargada del manejo de certificados, desde su creación y emisión hasta su almacenamiento en un almacén
 * de claves. También se encarga de generar el certificado de la Autoridad Certificadora de la aplicación.
 * Los certificados emitidos para los hosts visitados por los usuarios son generados de forma dinámica y bajo demanda.
 * @author Pablo
 *
 * Notas importantes sobre las propiedades de certificados:
 * 
 * CN: CommonName, OU: OrganizationalUnit, O: Organization, L: Locality, ST:
 * State Or Province Name, C: CountryName, SAN: Subject Alternative Name
 *
 */
public class SSLManager {

	private X509Certificate CACertificate;
	private X509Certificate[] certsChain;
	private PrivateKey caPrivateKey;

	private Date startDate, endDate;

	private final String ksType = "JKS";

	private final String CAAlias = "proxyca";

	private final Lock queueLock = new ReentrantLock();
	private static SSLManager instance = new SSLManager();

	private final static Logger LOG = Logger.getLogger(SSLManager.class);
	
	private SSLManager() {
		setDates();

		Security.addProvider(new BouncyCastleProvider());
	}

	public static SSLManager getInstance() {
		return instance;
	}

	/**
	 * Método que almacena en dos propiedades distintas la fecha actual y la fecha
	 * de justo 10 años después desde este momento. Se utiliza para controlar la caducidad
	 * de certificados.
	 */
	private void setDates() {
		// ====== Validity Date =====
		// Actual Date
		long now = System.currentTimeMillis();
		startDate = new Date(now);

		// Adding 10 years to startDate
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.YEAR, 10);
		endDate = c.getTime();
	}

	/**
	 * Método que carga el certificado de la AC (o lo crea si no existe), y lo usa para
	 * firmar un nuevo certificado emitido para "localhost", necesario para las conexiones
	 * a través de HTTPS.
	 */
	public void generateCertificateForLocalhost() {
		generateCACertificate(false);
		generateEndEntityCertificate("localhost");
	}

	// We generate the CA Certificate with Bouncy Castle
	/** Método que crea el certificado de la Autoridad Certificadora en caso de que no exista (si existe, lo carga
	 * desde el almacén de claves).
	 * @param fileExistsButNotImportedIntoKS True si el fichero que contiene el certificado existe pero no se ha importado
	 * aún en el almacén de claves.
	 */
	public void generateCACertificate(boolean fileExistsButNotImportedIntoKS) {
		queueLock.lock();
		File caFile = new File("ProxyCACert.pem");
		if (!caFile.exists() || fileExistsButNotImportedIntoKS) {
			try {
				System.err.println("===> Creating CA Certificate! <===");
				// Create self signed Root CA certificate
				KeyPair CAkp = generateKeyPair();

				X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
						new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=ProxyTFGCA"), // issuer
																													// authority
						BigInteger.valueOf(new Random().nextInt()), // serial number of certificate
						startDate, // start of validity
						endDate, // end of certificate validity
						new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=ProxyTFGCA"), // subject
																													// name
																													// of
																													// certificate
						CAkp.getPublic()); // public key of certificate

				// key usage restrictions
				builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));
				builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(true));
				X509Certificate CACert = new JcaX509CertificateConverter().getCertificate(builder.build(
						new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(CAkp.getPrivate()))); // private
																													// key
																													// of
																													// signing
																													// authority
																													// ,
																													// here
																													// it
																													// is
																													// self
																													// signed

				CACertificate = CACert;
				certsChain = new X509Certificate[1]; // this array will store the CACertificate
				certsChain[0] = CACertificate;
				caPrivateKey = CAkp.getPrivate();

				saveCACertToFile();
				System.err.println("<=== CA Certificate created! ===>");

				saveCertToKeyStore(CAAlias, CAkp.getPrivate(), certsChain);
			} catch (CertificateException e) {
				LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
			} catch (OperatorCreationException e) {
				LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}

		} else {
			loadCAFromKeyStore();
		}
		queueLock.unlock();
	}

	/**
	 * Método que carga el certificado de la AC desde el almacén de claves.
	 */
	private void loadCAFromKeyStore() {

		InputStream in = null;
		try {
			in = getKeyStoreAsInputStream();

			KeyStore keyStore = KeyStore.getInstance(ksType);
			keyStore.load(in, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

			if (!keyStore.containsAlias(CAAlias)) {
				generateCACertificate(true);
			} else {
				caPrivateKey = (PrivateKey) keyStore.getKey(CAAlias,
						System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

				Certificate[] chain = keyStore.getCertificateChain(CAAlias);

				certsChain = new X509Certificate[chain.length];
				System.arraycopy(chain, 0, certsChain, 0, chain.length);

				CACertificate = certsChain[0];
			}

		} catch (IOException ioe) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + ioe.getMessage());
		} catch (KeyStoreException e) {
			LOG.log(Level.ERROR, "Error en la KeyStore. " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error con el algoritmo seleccionado para la keystore. " + e.getMessage());
		} catch (CertificateException e) {
			LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
		} catch (UnrecoverableKeyException e) {
			LOG.log(Level.ERROR, "Error al recuperar la clave de la keystore. " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}
	}

	/** Método que emite un certificado para un Host concreto. Lo firma la Autoridad Certificadora.
	 * @param hostname Host al que emitir el certificado.
	 */
	public void generateEndEntityCertificate(String hostname) {
		queueLock.lock();
		if (!isCertAlreadyGeneratedForHost(hostname)) {
			// create end user cert signed
			KeyPair endEntityCertKeyPair = generateKeyPair();

			X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(CACertificate, // issuer authority
																								// (CACert)
					BigInteger.valueOf(new Random().nextInt()), startDate, endDate,
					new X500Name("C=Oviedo, ST=Asturias, L=Spain, O=Proxy TFG, OU=Proxy TFG, CN=" + hostname), // subject
					endEntityCertKeyPair.getPublic());

			try {
				builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));

				// Add Subject Alternative Name (SAN).
				List<GeneralName> sanList = new ArrayList<GeneralName>();
				sanList.add(new GeneralName(GeneralName.dNSName, hostname));
				GeneralNames subjectAltNames = GeneralNames
						.getInstance(new DERSequence((GeneralName[]) sanList.toArray(new GeneralName[] {})));
				builder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);

				// Add more attributes to the End User Certificate.
				builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
				X509Certificate endUserCert;
				endUserCert = new JcaX509CertificateConverter().getCertificate(builder
						.build(new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(caPrivateKey))); // private
																														// key
																														// of
																														// CA
				X509Certificate[] certsChain = getCertChain(endUserCert);
				saveCertToKeyStore(hostname, endEntityCertKeyPair.getPrivate(), certsChain);
			} catch (CertificateException e) {
				LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
			} catch (OperatorCreationException e) {
				LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
			} catch (CertIOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida relacionado con el certificado. " + e.getMessage());
			}

		}
		queueLock.unlock();

	}

	private X509Certificate[] getCertChain(X509Certificate userCert) {
		X509Certificate[] chain = new X509Certificate[certsChain.length + 1];
		System.arraycopy(certsChain, 0, chain, 1, certsChain.length);
		chain[0] = userCert;
		return chain;
	}

	/** Método que comprueba si se ha generado ya un certificado para un Host concreto.
	 * @param hostname Host al que se le ha emitido un certificado.
	 * @return True si el certificado del Host se encuentra en el almacén de claves, False en otro caso.
	 */
	private boolean isCertAlreadyGeneratedForHost(String hostname) {
		InputStream is = null;
		boolean contains = false;
		try {
			is = getKeyStoreAsInputStream();

			KeyStore keystore = KeyStore.getInstance(ksType);
			keystore.load(is, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

			if (keystore.containsAlias(hostname)) {
				contains = true;
			}
		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Error al buscar el fichero. " + e.getMessage());
		} catch (KeyStoreException e) {
			LOG.log(Level.ERROR, "Error con la keystore. " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error al seleccionarl el algoritmo para la keystore. " + e.getMessage());
		} catch (CertificateException e) {
			LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}

		return contains;
	}

	private /* static */ KeyPair generateKeyPair() {
		KeyPairGenerator kpGen = null;
		try {
			kpGen = KeyPairGenerator.getInstance("RSA", "BC");
			kpGen.initialize(1024);
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error al seleccionar algoritmo. " + e.getMessage());
		} catch (NoSuchProviderException e) {
			LOG.log(Level.ERROR, "Error al seleccionar el proveedor para generar claves. " + e.getMessage());
		}
		return kpGen.generateKeyPair();
	}

	/**
	 * Método que almacena en fichero el contenido del certificado de la Autoridad Certificadora, codificado
	 * en formato Base64.
	 */
	private void saveCACertToFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter("ProxyCACert.pem");
			fw.write(getCACert());
			fw.flush();
		} catch (CertificateEncodingException e) {
			LOG.log(Level.ERROR, "Error de codificación de certificados. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}
	}

	/** Método que guarda un certificado en el almacén de claves.
	 * @param hostnameAsAlias Host para el que se emitirá el certificado.
	 * @param certPrivKey Clave privada del certificado a guardar.
	 * @param certsChain Cadena de certificados (certificado de la AC y el certificado emitido para el Host).
	 */
	private void saveCertToKeyStore(String hostnameAsAlias, PrivateKey certPrivKey, X509Certificate[] certsChain) {
		File file = getKeyStoreAsFile();
		InputStream is = null;
		try {
			// Load the Java Default KeyStore content into an auxiliar "keystore"
			is = getKeyStoreAsInputStream();
			
			KeyStore keystore = KeyStore.getInstance(ksType);
			keystore.load(is, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

			if (keystore.containsAlias(hostnameAsAlias)) {
				keystore.deleteEntry(hostnameAsAlias);
			}
			// Add the certificate to the auxiliar keystore
			keystore.setKeyEntry(hostnameAsAlias, certPrivKey,
					System.getProperty("javax.net.ssl.keyStorePassword").toCharArray(), certsChain);

			// Save the new keystore content from the auxiliar to the "real" Java Default
			// KeyStore
			OutputStream out = new FileOutputStream(file);
			keystore.store(out, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			out.close();

		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Error al buscar el fichero. " + e.getMessage());
		} catch (KeyStoreException e) {
			LOG.log(Level.ERROR, "Error con la keystore. " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error al seleccionarl el algoritmo para la keystore. " + e.getMessage());
		} catch (CertificateException e) {
			LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}

	}

	/** Método que crea un contexto para la comunicación mediante SSL con un Host.
	 * Un contexto es utilizado para comunicarse estableciendo previamente los parámetros de comunicación
	 * como, por ejemplo, los certificados en los que se confía, los protocolos de cifrado
	 * admitidos, etc.
	 * @param host Host con el que se realizará la comunicación.
	 * @return El contexto SSL.
	 */
	public SSLContext generateContext(String host) {
		queueLock.lock();
		KeyManagerFactory kmf;
		try {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

			KeyStore ks = getKeyStore();
			kmf.init(ks, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(ks);

			PrivateKey pk = (PrivateKey) ks.getKey(host,
					System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
			Certificate[] chain = ks.getCertificateChain(host);
			X509Certificate[] certChain = new X509Certificate[chain.length];
			System.arraycopy(chain, 0, certChain, 0, chain.length);

			SingleKeyManager km = new SingleKeyManager(host, pk, certChain);

			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(new KeyManager[] { km }, tmf.getTrustManagers(), new SecureRandom());
			queueLock.unlock();

			return sslContext;
		} catch (KeyStoreException e) {
			LOG.log(Level.ERROR, "Error con la keystore. " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error al seleccionarl el algoritmo para la keystore. " + e.getMessage());
		} catch (UnrecoverableKeyException e) {
			LOG.log(Level.ERROR, "Error al recuperar clave de la keystore. " + e.getMessage());
		} catch (KeyManagementException e) {
			LOG.log(Level.ERROR, "Error con las claves de la keystore. " + e.getMessage());
		}
			
		return null;
	}

	/** Método que devuelve una referencia al almacén de claves.
	 * @return KeyStore.
	 */
	private KeyStore getKeyStore() {
		InputStream in = null;
		KeyStore ks = null;
		try {
			in = getKeyStoreAsInputStream();
			ks = KeyStore.getInstance(ksType);
			ks.load(in, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Error al buscar el fichero. " + e.getMessage());
		} catch (KeyStoreException e) {
			LOG.log(Level.ERROR, "Error con la keystore. " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.ERROR, "Error al seleccionarl el algoritmo para la keystore. " + e.getMessage());
		} catch (CertificateException e) {
			LOG.log(Level.ERROR, "Error al generar el certificado. " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			}
		}
		return ks;
	}

	/** Método que devuelve el certificado de la AC codificado en Base64.
	 * @return Certificado de la AC codificado en Base64.
	 * @throws CertificateEncodingException Cuando no se ha podido codificar una
	 * cadena concreta.
	 */
	private String getCACert() throws CertificateEncodingException {
		Base64.Encoder encoder = Base64.getEncoder();
		return "-----BEGIN CERTIFICATE-----\n" + encoder.encodeToString(certsChain[0].getEncoded())
				+ "\n-----END CERTIFICATE-----";
	}

	private InputStream getKeyStoreAsInputStream() {
		String cacerts = System.getProperty("javax.net.ssl.keyStore");
		InputStream is=null;
		try {
			is = new FileInputStream(ResourceUtils.getFile(cacerts));
		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Error al buscar el fichero. " + e.getMessage());
		}
		return is;
	}
	
	private File getKeyStoreAsFile() {
		String cacerts = System.getProperty("javax.net.ssl.keyStore");
		File file=null;
		try {
			file = ResourceUtils.getFile(cacerts);
		} catch (FileNotFoundException e) {
			LOG.log(Level.ERROR, "Error al buscar el fichero. " + e.getMessage());
		}
		return file;
	}
	
}

class DefaultTrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
