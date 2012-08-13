package aider.org.pmsiadmin.controller;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.springframework.web.servlet.ModelAndView;

public class UtilsController<T> {

	public ModelAndView checkGetParams(T params, Validator validator) {
		Set<ConstraintViolation<T>> constraintViolations =
				validator.validate(params);
			
	if (constraintViolations.size() > 0 ) {
		String violations = "";
		for (ConstraintViolation<T> contraintes : constraintViolations) {
			if (contraintes.getPropertyPath().toString().equals("session"))
				return new ModelAndView("redirect:/Authentification/Form");
				violations += contraintes.getRootBeanClass().getSimpleName() + "." +
						contraintes.getPropertyPath() + " " + contraintes.getMessage() + "\n";
		}
		throw new ValidationException("Erreurs de validation : " + violations);
	}
	
	return null;
	}
}
