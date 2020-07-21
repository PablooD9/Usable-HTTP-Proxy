package com.proxy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proxy.configuration.MongoConfiguration;
import com.proxy.model.Configuration;
import com.proxy.model.User;
import com.proxy.services.ConfigurationService;
import com.proxy.services.HostService;
import com.proxy.services.LanguageService;
import com.proxy.services.UserService;

/** Clase encargada de enviar al usuario las vistas correspondientes a los recursos
 * que solicita relacionados con la pantalla de configuración de la aplicación.
 * @author Pablo
 *
 */
@Controller
public class ConfigurationController {
	
	/**
	 * Servicios.
	 */
	@Autowired
	private HostService hostService;
	@Autowired
	private UserService userService;
	@Autowired
	private LanguageService langService;
	@Autowired
	private ConfigurationService confService;
	@Autowired
	private MongoConfiguration mongoConfiguration;
	
	/** Método que permite mostrar al usuario la vista correspondiente a la pantalla de configuración.
	 * @param language Lenguaje de la aplicación.
	 * @param model Objeto en el que se incluirán atributos que posteriormente procesará la vista.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value={"", "/", "/configuration"})
	public String getConfiguration(@RequestParam(required = false, name = "lang") String language, Model model) {
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		
		User userLoggedIn = userService.getUserLoggedIn();
		Configuration userConfig = confService.getConfigOfUser();
		
		model.addAttribute("userLoggedIn", userLoggedIn);
		model.addAttribute("userConfig", userConfig);
		model.addAttribute("OSOptions", confService.getOSOptions());
		model.addAttribute("BrowserOptions", confService.getBrowserOptions());
		model.addAttribute("securityHeaders", confService.getSecurityHeaders());
		model.addAttribute("hostExceptions", confService.getExceptionsList( null ));
		
		return "configuration/configuration";
	}
	
	/** Método que actualiza la lista de hosts de la aplicación.
	 * @return Redirige al usuario a la pantalla de configuración.
	 */
	@RequestMapping("/updateMaliciousHosts")
	public String updateMaliciousHosts() {
		hostService.updateHostsList();
		return "redirect:/";
	}
	
	/** Método que guarda las preferencias de los usuarios de la pantalla de configuración.
	 * @param op1_os Sistema operativo (opción 1)
	 * @param op1_browser Navegador (opción 1)
	 * @param op2 Bloquear contenido malicioso.
	 * @param op3 Bloquear contenido malicioso ubicado en España.
	 * @param op4 Bloquear contenido con fines de rastreo.
	 * @param op5 Bloquear contenido pornográfico.
	 * @param op6 Cabeceras de seguridad.
	 * @param op7 Cookies.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value = "/savePreferences", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String savePreferences(@RequestParam("_op1_os") String op1_os, 
						 @RequestParam("_op1_browser") String op1_browser,
						 @RequestParam("_op2") String op2,
						 @RequestParam("_op3") String op3,
						 @RequestParam("_op4") String op4,
						 @RequestParam("_op5") String op5,
						 @RequestParam("_op6") String op6,
						 @RequestParam("_op7") String op7) 
	{	
		Configuration configuration = confService.buildConfigurationObject(op1_os, op1_browser, op2, op3, op4, op5, op6, op7);
		confService.saveConfiguration(configuration);
		
	    return "";
	}
	
	/** Método que añade una excepción de seguridad a un Host en concreto.
	 * @param host Host al que añadir la excepción de seguridad.
	 * @param model Objeto en el que se incluirán atributos que posteriormente procesará la vista.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping("/addSecurityException")
	public String addSecurityException(@RequestParam("host") String host, Model model) {
		boolean exceptionAdded = confService.saveSecurityException(host);
		
		model.addAttribute("lang", langService.getActualLocaleLang( null ));
		model.addAttribute("host", host);
		model.addAttribute("securityExceptionExists", exceptionAdded);
		
		return "configuration/addSecurityException";
	}
	
	/** Método que elimina una excepción de seguridad de un Host en concreto.
	 * @param host Host al que quitarle la excepción de seguridad previamente añadida.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value = "/deleteSecurityException", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String deleteSecurityException(@RequestParam("host") String host) {
		confService.deleteSecurityException(host);
		
		return "";
	}
	
	@RequestMapping(value = "/deleteAllDBTest", method = RequestMethod.GET)
	@ResponseBody
	public String deleteAllDBTest() {
		mongoConfiguration.deleteAllDBTest();
		
		return "";
	}

	public void setConfService(ConfigurationService confServiceMock) {
		this.confService = confServiceMock;
	}
	
}
