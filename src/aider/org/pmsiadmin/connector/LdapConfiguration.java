package aider.org.pmsiadmin.connector;

public class LdapConfiguration {

	private String ldapAuthentication;
	
	private String ldapHost;
	
	private String ldapDn;

	private String ldapObjectClass;
	
	private String ldapUserlogin;
	
	private String ldapAdditionalFilters;
	
	private String ldapMappingUniqueUserId;
	
	private String ldapMappingSurname;
	
	private String ldapMappingGivenName;
	
	private String ldapMappingMail;

	public String getLdapAuthentication() {
		return ldapAuthentication;
	}

	public void setLdapAuthentication(String ldapAuthentication) {
		this.ldapAuthentication = ldapAuthentication;
	}
	
	/**
	 * Utilise les informations de {@link LdapConfiguration#ldapAuthentication} pour
	 * renvoyer l'identifiant qu'attend l'implémentation de ldap sous-jacente
	 * @param user le login de l'utilisateur
	 * @return
	 */
	public String getProcessedAuthIdentifier(String user) {
		// TODO : Il existe un risque de sécurité à l'injection de ldap, il faudrait
		// limiter les caractères acceptés en entrée
		String processed = new String(getLdapAuthentication());
		processed = processed.replaceAll("%u", user);
		processed = processed.replaceAll("%d", getLdapDn());
		return processed;
	}

	public String getLdapHost() {
		return ldapHost;
	}

	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	public String getLdapDn() {
		return ldapDn;
	}

	public void setLdapDn(String ldapDn) {
		this.ldapDn = ldapDn;
	}

	public String getLdapObjectClass() {
		return ldapObjectClass;
	}

	public void setLdapObjectClass(String ldapObjectClass) {
		this.ldapObjectClass = ldapObjectClass;
	}

	public String getLdapUserlogin() {
		return ldapUserlogin;
	}

	public void setLdapUserlogin(String ldapUserlogin) {
		this.ldapUserlogin = ldapUserlogin;
	}

	public String getLdapAdditionalFilters() {
		return ldapAdditionalFilters;
	}

	public void setLdapAdditionalFilters(String ldapAdditionalFilters) {
		this.ldapAdditionalFilters = ldapAdditionalFilters;
	}

	public String getLdapMappingUniqueUserId() {
		return ldapMappingUniqueUserId;
	}

	public void setLdapMappingUniqueUserId(String ldapMappingUniqueUserId) {
		this.ldapMappingUniqueUserId = ldapMappingUniqueUserId;
	}

	public String getLdapMappingSurname() {
		return ldapMappingSurname;
	}

	public void setLdapMappingSurname(String ldapMappingSurname) {
		this.ldapMappingSurname = ldapMappingSurname;
	}

	public String getLdapMappingGivenName() {
		return ldapMappingGivenName;
	}

	public void setLdapMappingGivenName(String ldapMappingGivenName) {
		this.ldapMappingGivenName = ldapMappingGivenName;
	}

	public String getLdapMappingMail() {
		return ldapMappingMail;
	}

	public void setLdapMappingMail(String ldapMappingMail) {
		this.ldapMappingMail = ldapMappingMail;
	}
}
