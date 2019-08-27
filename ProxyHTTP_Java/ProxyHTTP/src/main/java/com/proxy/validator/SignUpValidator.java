package com.proxy.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.proxy.model.User;
import com.proxy.services.UserService;

@Component
public class SignUpValidator implements Validator {

	@Autowired
	private UserService userService;

	@Override
	public boolean supports(Class<?> arg0) {
		return User.class.equals(arg0);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User) target;
		boolean existEmailErrors = false;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "Error.emptyEmail");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Error.emptyName");
		if (errors.hasErrors())
			existEmailErrors = true;
		
		if(user.getName().length() > 15) {
			errors.rejectValue("name", "Error.nameLength");
		}
		if(!user.getEmail().contains( "@" ) && !existEmailErrors) {
			errors.rejectValue("email", "Error.invalidEmail");
			existEmailErrors = true;
		}
		if (userService.findUserByEmail(user.getEmail()) != null && !existEmailErrors) {
			errors.rejectValue("email", "Error.emailExists");
			existEmailErrors = true;
		}
		if (user.getPassword().length() < 6) {
			errors.rejectValue("password", "Error.passLength");
		}
		
	}
	
	
}
