package aider.org.pmsiadmin.model.get;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import aider.org.pmsiadmin.model.ldap.Session;
import aider.org.pmsiadmin.validator.IsIntegerTransformer;

@IsIntegerTransformer(source="numIndex", destination="numIndexI")
public class PmsiListGetParamModel {

	// ==== Définis par l'utilisateur ====
	@NotNull
	private String numIndex = null;
	
	@Pattern(regexp="[0-9]{9}")
	private String numFiness = null;
	
	// ==== Définis par le controlleur ====
	
	@NotNull
	private Session session = null;
	
	// ==== Définis par le validateur ====
	
	private Integer numIndexI = null;	
		
	public PmsiListGetParamModel(String numIndex, String numFiness, Session session) {
		setNumIndex(numIndex);
		setSession(session);
		setNumFiness(numFiness);
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

	public String getNumFiness() {
		return numFiness;
	}

	public void setNumFiness(String numFiness) {
		this.numFiness = numFiness;
	}

	public Integer getNumIndexI() {
		return numIndexI;
	}

}
