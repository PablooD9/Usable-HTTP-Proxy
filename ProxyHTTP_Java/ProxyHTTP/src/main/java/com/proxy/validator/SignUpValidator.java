package com.proxy.validator;

import java.util.regex.Pattern;

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
		user.setName(user.getName().trim());
		boolean existEmailErrors = false;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "Error.emptyEmail");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Error.emptyName");
		if (errors.hasErrors())
			existEmailErrors = true;
		
		if(user.getName().length() > 15) {
			errors.rejectValue("name", "Error.nameLength");
		}
		else {
			String patternAttacker = "[a-zA-Z0-9]+";
			if(!Pattern.compile(patternAttacker).matcher(user.getName()).find() && !existEmailErrors) {
				errors.rejectValue("name", "Error.invalidName");
			}
		}
		String emailVerification = "[a-zA-Z0-9_]+@[a-zA-Z0-9.]+";
		if(!Pattern.compile(emailVerification).matcher(user.getEmail()).find() && !existEmailErrors) {
			errors.rejectValue("email", "Error.invalidEmail");
			existEmailErrors = true;
		}
		if(user.getEmail().length() > 50 && !existEmailErrors) {
			errors.rejectValue("email", "Error.emailLength");
			existEmailErrors = true;
		}
		if (userService.findUserByEmail(user.getEmail()) != null && !existEmailErrors) {
			errors.rejectValue("email", "Error.emailExists");
			existEmailErrors = true;
		}
		if (user.getPassword().length() < 6 || user.getPassword().length() > 23) {
			errors.rejectValue("password", "Error.passLength");
		}
		else if (!Pattern.compile("\\d+").matcher(user.getPassword()).find()){
			errors.rejectValue("password", "Error.passNumber");
		}
		
	}
	
	
}
