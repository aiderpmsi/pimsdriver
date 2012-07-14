package aider.org.pmsiadmin.validator;

import javax.naming.NamingException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import aider.org.pmsiadmin.model.form.AuthenticationForm;
import aider.org.pmsiamin.model.ldap.Session;

/**
 * Classe faisant le travail de validation
 * @author delabre
 *
 */
public class AuthenticationFormValidator implements ConstraintValidator<AuthenticationFormValidatorAnnotation, AuthenticationForm> {

	@Override
	public void initialize(AuthenticationFormValidatorAnnotation authenticationFormValidatorAnnotation) {
	}

	@Override
	public boolean isValid(AuthenticationForm authenticationForm, ConstraintValidatorContext constraintValidatorContext) {
	
		Session session = null;
		
		// Récupère une session, si possible, sinon si c'est pas
		// possible, renvoie que le form est pas valide
		try {
			session = authenticationForm.getDtoSession().getSession(
					authenticationForm.getUser(), authenticationForm.getPass());
		} catch (NamingException e) { }
		
		authenticationForm.setSession(session);

		if (session != null)
			return true;
		else
			return false;
	}
}
