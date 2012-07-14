package aider.org.pmsiamin.model.ldap;

/**
 * Objet de session contenant les identifiants de l'utilisateur connecté.
 * C'est un bean simple
 * @author delabre
 *
 */
public class Session {
	
	private String user;
	
	private String userGUID;
	
	private String givenName;
	
	private String sn;
	
	private String mail;

	// =================== Getters et Setters ==================
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUserGUID() {
		return userGUID;
	}

	public void setUserGUID(String userGUID) {
		this.userGUID = userGUID;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
}
