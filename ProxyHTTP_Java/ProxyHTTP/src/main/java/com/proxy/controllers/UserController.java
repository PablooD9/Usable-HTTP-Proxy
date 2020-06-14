package com.proxy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.proxy.model.User;
import com.proxy.services.LanguageService;
import com.proxy.services.UserService;
import com.proxy.validator.SignUpValidator;

/** Clase encargada de enviar al usuario las vistas correspondientes a los recursos
 * que solicita relacionados con las pantallas de inicio de sesión y registro de la aplicación.
 * @author Pablo
 *
 */
@Controller
public class UserController {
	@Autowired
	private SignUpValidator signupValidator;
	
	/**
	 * Servicios.
	 */
	@Autowired
	private LanguageService langService;
	@Autowired
	private UserService userService;
	
	/** Método que devuelve al usuario la vista de la página de inicio de sesión.
	 * @param language Lenguaje de la aplicación.
	 * @param error Indica si el proceso de autenticación ha fallado.
	 * @param model Objeto en el que se incluirán atributos que posteriormente procesará la vista.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(required = false, name = "lang") String language, 
						@RequestParam(required = false, name = "error") String error,
						Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		if (error != null)
			model.addAttribute("error", error);
		else
			model.addAttribute("error", "false");
		
		return "login/login";
	}
	
	@RequestMapping(value = "/login/error", method = RequestMethod.GET)
	public String loginError() {
		return "redirect:/login?error=true";
	}
	
	/** Método que devuelve al usuario la vista de la página de registro.
	 * @param language Lenguaje de la aplicación.
	 * @param model Objeto en el que se incluirán atributos que posteriormente procesará la vista.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signup(@RequestParam(required = false, name = "lang") String language, Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("lang", langService.getActualLocaleLang( language ));
		
		return "login/register";
	}
	
	/** Método que, si el proceso de registro ha ido correctamente, guarda al usuario registrado en la
	 * base de datos y le redirige a la página de inicio de sesión. En caso contrario, le devuelve a
	 * la página de registro, indicando por pantalla los campos inválidos.
	 * @param user Objeto que representa a un usuario.
	 * @param result Objeto que valida si se han cometido errores en el proceso de registro.
	 * @param model Objeto en el que se incluirán atributos que posteriormente procesará la vista.
	 * @return Ubicación de la vista.
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String signup(@ModelAttribute @Validated User user, BindingResult result, Model model) {
		signupValidator.validate(user, result);
		if(result.hasErrors()) {
			model.addAttribute("user", user);
			model.addAttribute("lang", langService.getActualLocaleLang( null ));
			return "login/register";
		}
		userService.saveUser(user);
		
		return "redirect:/login";
	}
	
	/** Método para las pruebas con Mockito.
	 * @param userServiceMock Mockito de UserService.
	 */
	public void setUserService(UserService userServiceMock) {
		this.userService = userServiceMock;
	}
}
