package aider.org.pmsiadmin.model.form;

import aider.org.pmsi.validator.AuthenticationFormValidatorAnnotation;
import aider.org.pmsiamin.model.ldap.DtoSession;
import aider.org.pmsiamin.model.ldap.Session;

/**
 * Bean permettant de stocker les valeurs du formulaire de recherche de patients
 * @author delabre
 *
 */
@AuthenticationFormValidatorAnnotation
public class AuthenticationForm{
 
	private String user;
	
	private String pass;
	
	private DtoSession dtoSession;
	
	private Session session;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public DtoSession getDtoSession() {
		return dtoSession;
	}

	public void setDtoSession(DtoSession dtoSession) {
		this.dtoSession = dtoSession;
	}
	
}