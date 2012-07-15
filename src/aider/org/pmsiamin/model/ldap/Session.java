package aider.org.pmsiamin.model.ldap;

/**
 * Objet de session contenant les identifiants de l'utilisateur connecté.
 * C'est un bean simple
 * @author delabre
 *
 */
public class Session {
	
	private String userLogin;
	
	private String uniqueUserId;
	
	private String givenName;
	
	private String surname;
	
	private String mail;

	// =================== Getters et Setters ==================
	
	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUniqueUserId() {
		return uniqueUserId;
	}

	public void setUniqueUserId(String uniqueUserId) {
		this.uniqueUserId = uniqueUserId;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
}
