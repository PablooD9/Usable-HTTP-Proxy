package com.proxy.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proxy.model.Configuration;
import com.proxy.model.SecurityException;
import com.proxy.model.UserConfiguration;
import com.proxy.model.hosttype.Host;
import com.proxy.model.option.DefaultOption;
import com.proxy.model.option.DefaultOptionBrowser;
import com.proxy.model.option.DefaultOptionOS;
import com.proxy.model.option.DefaultOptionSecurityHeader;
import com.proxy.model.option.DefaultOptionUserAgent;
import com.proxy.model.option.Option;
import com.proxy.model.option.OptionUserAgent;
import com.proxy.repository.ConfigurationRepository;
import com.proxy.repository.SecurityExceptionRepository;

/** Servicio encargado de cargar en la aplicación las opciones de configuración
 * de los distintos ficheros, y de guardar las configuraciones establecidas por los usuarios.
 * @author Pablo
 *
 */
@Service
public class ConfigurationService {

	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationRepository confRepository;
	@Autowired
	private HostService hostService;
	@Autowired
	private SecurityExceptionRepository secExceptionRepository;
	
	private final static String OS_FILE_PATH = "src/main/resources/static/otherFiles/OS.txt";
	private List<Option> OSOptions;
	
	private final static String BROWSER_FILE_PATH = "src/main/resources/static/otherFiles/Browsers.txt";
	private List<Option> browserOptions;
	
	private final static String SECURITY_HEADERS_FILE_PATH = "src/main/resources/static/otherFiles/SecurityHeaders.txt";
	private List<Option> securityHeaders;
	
	private final static String USER_AGENT_FILE_PATH = "src/main/resources/static/otherFiles/UAConfiguration.txt";
	private List<Option> UAOptions;
	
	private final static String[] OPTION_TYPES = new String[] { "Default", "User-Agent" };
	
	private final static Logger LOG = Logger.getLogger(ConfigurationService.class);
	
	private boolean configurationIsActive = false;
	
	public List<Option> getOSOptions(){ return OSOptions; }
	public List<Option> getBrowserOptions(){ return browserOptions; }
	public List<Option> getUAOptions(){ return UAOptions; }
	public List<Option> getSecurityHeaders(){ return securityHeaders; }
	
	/**
	 * Carga las opciones de la aplicación a partir de los distintos ficheros.
	 */
	@PostConstruct
	private void loadOptionsInfo() {
		OSOptions = readOptionsFile(OS_FILE_PATH, OPTION_TYPES[0]);
		browserOptions = readOptionsFile(BROWSER_FILE_PATH, OPTION_TYPES[0]);
		securityHeaders = readOptionsFile(SECURITY_HEADERS_FILE_PATH, OPTION_TYPES[0]);
		UAOptions = readOptionsFile(USER_AGENT_FILE_PATH, OPTION_TYPES[1]);
		
		if (optionsAreNull())
			loadDefaultOptions();
	}
	
	private boolean optionsAreNull() {
		if (OSOptions == null || browserOptions == null || UAOptions == null || securityHeaders == null)
			return true;
		
		return false;
	}
	
	/** Devuelve una lista de opciones a partir de la ruta de un fichero.
	 * @param file_path Ruta al fichero que contiene las opciones.
	 * @param optionType Tipo de la opción a leer.
	 * @return Lista de opciones.
	 */
	private List<Option> readOptionsFile(String file_path, String optionType){
		List<Option> optionsList = new ArrayList<>();
		
		File file = new File(file_path);
		if (!file.exists()) {
			return null;
		}
		else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = "";
				while ((line = reader.readLine()) != null) {
					Option option = Option.parseOptionLine(line, optionType);
					if (option.parse() == true) {
						optionsList.add( option );
					}
				}
			} catch (FileNotFoundException e) {
				LOG.log(Level.ERROR, "Fichero no encontrado. " + e.getMessage());
			} catch (IOException e) {
				LOG.log(Level.ERROR, "Error de entrada/salida. " + e.getMessage());
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						LOG.log(Level.ERROR, "Error al cerrar el bufferedreader. " + e.getMessage());
					}
				}
			}
		}
		
		return optionsList;
	}
	
	/** Construye y devuelve un objeto de tipo Configuration a partir de las opciones
	 * establecidas por el usuario desde la página de configuración de la aplicación.
	 * @param op1_os Sistema operativo.
	 * @param op1_browser Navegador.
	 * @param op2 Opción 2.
	 * @param op3 Opción 3.
	 * @param op4 Opción 4.
	 * @param op5 Opción 5.
	 * @param op6 Opción 6.
	 * @return Configuración.
	 */
	public Configuration buildConfigurationObject(String op1_os, String op1_browser, String op2_spanish_hosts,
												  String op3_malicious_hosts, String op4_tracker_hosts, String op5_porn_hosts, String op6_sec_headers, String op7_cookie_headers) {
		String userEmail = userService.getEmailOfLoggedInUser();
		Configuration configuration = new Configuration(userEmail, op1_os, op1_browser, op2_spanish_hosts, op3_malicious_hosts, op4_tracker_hosts, op5_porn_hosts, op6_sec_headers, op7_cookie_headers);
		configuration.setHostExceptions(UserConfiguration.getInstance().getConfiguration().getHostExceptions());
		return configuration;
	}
	
	/** Guarda en la aplicación y en la base de datos (este último solo si el usuario está autenticado)
	 * la excepción de seguridad aplicada sobre un Host.
	 * @param host Host al que aplicar la excepción de seguridad.
	 * @return True si se ha guardado correctamente, False en otro caso.
	 */
	public boolean saveSecurityException(String host) {
		String email = userService.getEmailOfLoggedInUser();
		SecurityException secException = secExceptionRepository.findByEmail( email );
		List<String> secExceptionList = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		if (secException == null) {
			secException = new SecurityException(email, host);
			if (exceptionIsInExceptionsList(secExceptionList, host))
				return false;
			secExceptionList.add(host);
			if (userService.userIsLoggedIn())
				secExceptionRepository.save( secException );
		}
		else {
			if (secException.getHostsException().toLowerCase().contains(host)) // If host is already added to the exceptions, we don't add it again
				return false;
			secExceptionList.add(host);
			updateDatabaseHostsExceptionOfUser( secExceptionList, secException );
		}
		
		return true; // We have added the host exception
	}
	
	public void deleteSecurityException(String host) {
		List<String> secExceptionList = UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		if (exceptionIsInExceptionsList(secExceptionList, host)) { // If host is in user configuration list, we delete it 
			secExceptionList = deleteSecurityExceptionFromList(secExceptionList, host);
			UserConfiguration.getInstance().getConfiguration().setHostExceptions(secExceptionList);
			
			if (userService.userIsLoggedIn()) { // If user is logged in, we must delete this security exception from DB
				SecurityException secException = secExceptionRepository.findByEmail( userService.getEmailOfLoggedInUser() );
				if (secException != null) {
					updateDatabaseHostsExceptionOfUser(secExceptionList, secException);
				}
			}
		}
	}
	
	private boolean exceptionIsInExceptionsList(List<String> secExceptionList, String host) {
		if (secExceptionList == null)
			return true;
		for (String hostException : secExceptionList) {
			if (hostException.toLowerCase().equalsIgnoreCase(host))
				return true; // If host is already added to the exceptions, we don't add it again
		}
		
		return false;
	}
	
	private List<String> deleteSecurityExceptionFromList(List<String> secExceptionList, String host) {
		List<String> secExceptionLinkedList = new LinkedList<>();
		secExceptionLinkedList.addAll(secExceptionList);
		
		int counter=0;
		for (String hostException : secExceptionList) {
			if (hostException.toLowerCase().equalsIgnoreCase(host)) {
				secExceptionLinkedList.remove(counter);
			}
			counter++;
		}
		
		secExceptionList = new ArrayList<>();
		secExceptionList.addAll( secExceptionLinkedList );
		
		return secExceptionList;
	}
	
	private void updateDatabaseHostsExceptionOfUser( List<String> seList, SecurityException se) {
		String hostsException = "";
		int counter=0;
		for (String sException : seList) {
			if (counter++ == seList.size()-1)
				hostsException += sException;
			else
				hostsException += sException + ",";
		}
		se.setHostsException(hostsException);
		if (seList.isEmpty())
			secExceptionRepository.delete( se );
		else
			secExceptionRepository.save( se );
	}
	
	/** Método que guarda en la aplicación y en la base de datos (si el usuario está autenticado) la
	 * configuración establecida por el mismo.
	 * @param configuration Configuración a guardar.
	 */
	public void saveConfiguration(Configuration configuration) {
		UserConfiguration.getInstance().setConfiguration( configuration );
		String ua = getUserAgent(configuration);
		configuration.setUserAgent( ua );
		if (userService.userIsLoggedIn())
			confRepository.save( configuration );
		configurationIsActive = true;
		loadHostsFromFile(configuration);
	}
	
	private void loadHostsFromFile(Configuration config) {
		UserConfiguration.getInstance().setMaliciousHostsToScan(new ArrayList<Host>());
		List<Host> hostsToScan = hostService.getHostsFromActiveOptions();
		UserConfiguration.getInstance().getMaliciousHostsToScan().addAll(hostsToScan);
	}
	
	public Configuration getConfigOfUser() {
		String emailUser = userService.getEmailOfLoggedInUser();
		Optional<Configuration> optionalConf = Optional.empty();
		SecurityException secException = null;
		if (emailUser != null) {
			optionalConf = confRepository.findById( emailUser );
			secException = secExceptionRepository.findByEmail( emailUser );
		}
		List<String> secExceptionList = new ArrayList<>();
		if (secException != null) {
			secExceptionList = getExceptionsList( secException );
		}
		if (!optionalConf.isEmpty()) {
			UserConfiguration.getInstance().setConfiguration(optionalConf.get());
			UserConfiguration.getInstance().getConfiguration().setHostExceptions(secExceptionList);
			return optionalConf.get();
		}
		else if (configurationIsActive) { // Anonymous user
			return UserConfiguration.getInstance().getConfiguration();
		}
		else if (!secExceptionList.isEmpty()) {
			Configuration configuration = new Configuration();
			configuration.setHostExceptions(secExceptionList);
			return configuration;
		}
			
		return null;
	}
	
	private void loadDefaultOptions(){
		DefaultOption defOSOption = new DefaultOptionOS(OS_FILE_PATH);
		OSOptions = defOSOption.getOptions();
		
		DefaultOption defBrowserOption = new DefaultOptionBrowser(BROWSER_FILE_PATH);
		browserOptions = defBrowserOption.getOptions();
		
		DefaultOption defSecurityHeaders = new DefaultOptionSecurityHeader(SECURITY_HEADERS_FILE_PATH);
		securityHeaders = defSecurityHeaders.getOptions();
		
		DefaultOption defUAOption = new DefaultOptionUserAgent(USER_AGENT_FILE_PATH);
		UAOptions = defUAOption.getOptions();
	}
	
	/** Obtiene el valor de la cabecera User-Agent correspondiente al sistema operativo
	 * y navegador establecidos en la configuración del usuario.
	 * @param configuration Configuración.
	 * @return Valor de la cabecera User-Agent.
	 */
	private String getUserAgent(Configuration configuration) {
		String OS, browser;
		if (configuration != null) {
			OS = configuration.getOS();
			browser = configuration.getBrowser();
		}
		else {
			OS = UserConfiguration.getInstance().getConfiguration().getOS();
			browser = UserConfiguration.getInstance().getConfiguration().getBrowser();
		}
		
		if (OS.trim().equals("-") || browser.trim().equals("-")) { // Load the default User-Agent header given by browser.
			return null;
		}
		
		for (Option option : UAOptions) {
			if (option instanceof OptionUserAgent)
			{
				String ua = ((OptionUserAgent) option).getUserAgentIfValid(OS, browser);
				if (ua != null) {
					return ua;
				}
			}
		}
		
		return null;
	}
	
	public List<String> getExceptionsList(SecurityException se){
		if (se == null) {
			if (UserConfiguration.getInstance().getConfiguration() == null)
				return new ArrayList<>();
			
			return UserConfiguration.getInstance().getConfiguration().getHostExceptions();
		}
		String[] exceptions;
		if (se.getHostsException().contains(","))
			exceptions = se.getHostsException().split(",");
		else
			exceptions = new String[] { se.getHostsException() };
		
		List<String> list = new ArrayList<>();
		for (int i=0; i< exceptions.length; i++)
			list.add(exceptions[i]);
		
		return list;
	}
	
	
	public void setSecurityExceptionRepository(SecurityExceptionRepository secExcRepositoryMock) {
		this.secExceptionRepository = secExcRepositoryMock;
	}
	public void setConfRepository(ConfigurationRepository confRepositoryMock) {
		this.confRepository = confRepositoryMock;
	}
	
}
