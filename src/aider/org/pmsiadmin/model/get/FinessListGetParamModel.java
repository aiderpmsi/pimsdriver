package aider.org.pmsiadmin.model.get;

import javax.validation.constraints.NotNull;
import aider.org.pmsiadmin.model.ldap.Session;
import aider.org.pmsiadmin.validator.FinessListModelValid;

@FinessListModelValid
public class FinessListGetParamModel {

	// ==== Définis par l'utilisateur ====
	@NotNull
	private String numIndex = null;
	
	// ==== Définis par le controlleur ====
	
	@NotNull
	private Session session = null;
	
	// ==== Définis par le validateur ====
	
	private Integer numIndexI = null;	
		
	public FinessListGetParamModel(String numIndex, Session session) {
		setNumIndex(numIndex);
		setSession(session);
	}
	
	public String getNumIndex() {
		return numIndex;
	}

	public void setNumIndex(String numIndex) {
		this.numIndex = numIndex;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Integer getNumIndexI() {
		return numIndexI;
	}

}
