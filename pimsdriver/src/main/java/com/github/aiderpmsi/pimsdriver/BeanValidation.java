package com.github.aiderpmsi.pimsdriver;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("beanValidator")
public class BeanValidation implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		String valueS = (String) value;
		if (valueS == null || valueS.length() == 0) {
			 FacesMessage msg = new FacesMessage("Finess must be non null");
	            throw new ValidatorException(msg);
		}
	}

}
