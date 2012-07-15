package aider.org.pmsiadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Objet de configuration conservant tous le éléments saisis dans le fchier de config
 */
@Component("configuration")
public class Configuration {

	@Value("${sedna.host}")
	private String sednaHost;

	@Value("${sedna.user}")
	private String sednaUser;

	@Value("${sedna.pwd}")
	private String sednaPwd;
	
	@Value("${ldap.authentication}")
	private String ldapAuthentication;
		
	@Value("${ldap.host}")
	private String ldapHost;
	
	@Value("${ldap.dn}")
	private String ldapDn;

	@Value("${ldap.objectClass}")
	private String ldapObjectClass;
	
	@Value("${ldap.userlogin}")
	private String ldapUserlogin;
	
	@Value("${ldap.additionalFilters}")
	private String ldapAdditionalFilters;
	
	@Value("${ldap.mapping.UniqueUserId}")
	private String ldapMappingUniqueUserId;
	
	@Value("${ldap.mapping.Surname}")
	private String ldapMappingSurname;
	
	@Value("${ldap.mapping.givenName}")
	private String ldapMappingGivenName;
	
	@Value("${ldap.mapping.mail}")
	private String ldapMappingMail;
	
	// ============== Getters et Setters =====================
	
	public String getSednaHost() {
		return sednaHost;
	}

	public void setSednaHost(String sednaHost) {
		this.sednaHost = sednaHost;
	}

	public String getSednaUser() {
		return sednaUser;
	}

	public void setSednaUser(String sednaUser) {
		this.sednaUser = sednaUser;
	}

	public String getSednaPwd() {
		return sednaPwd;
	}

	public void setSednaPwd(String sednaPwd) {
		this.sednaPwd = sednaPwd;
	}
	
	public String getLdapAuthentication() {
		return ldapAuthentication;
	}

	public void setLdapAuthentication(String ldapAuthentication) {
		this.ldapAuthentication = ldapAuthentication;
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
